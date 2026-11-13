package io.github.nexfusion

import android.app.Application
import android.widget.Toast
import io.github.nexfusion.compat.XposedBridge
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import io.github.nexfusion.core.BuildConfig
import io.github.nexfusion.core.BundleClassLoader
import io.github.nexfusion.core.BundleManifest
import io.github.nexfusion.core.BundleParentClassLoader
import io.github.nexfusion.core.BundleRegistry
import io.github.nexfusion.core.InstalledBundle
import io.github.nexfusion.core.ModuleStatus
import io.github.nexfusion.core.PrefKeys
import io.github.nexfusion.core.getOrDefault
import io.github.nexfusion.core.XposedDexRemapper
import io.github.nexfusion.morphe.ResourceFinder
import io.github.nexfusion.morphe.resourceMappings
import java.io.File
import java.io.FileInputStream

class MainHook : XposedModule() {
    private var targetPackageName: String? = null
    @Volatile
    private var isHookApplied = false

    override fun onPackageReady(param: PackageReadyParam) {
        if (!param.isFirstPackage) return
        if (param.packageName == moduleApplicationInfo.packageName) {
            reportActive(param.classLoader)
            return
        }
        val remotePrefs = runCatching { getRemotePreferences(BundleRegistry.PREFS_GROUP) }.getOrNull()
        val ignored = remotePrefs?.let { runCatching { it.getStringSet(PrefKeys.IGNORED_PACKAGES, null) }.getOrNull() } ?: emptySet()
        if (param.packageName in ignored) return
        if (targetPackageName != null && targetPackageName != param.packageName) return

        val remoteBundles = remotePrefs?.let { runCatching { BundleRegistry.read(it) }.getOrNull() }
            ?: runCatching { BundleRegistry.read(getRemotePreferences(PrefKeys.BUNDLES_GROUP)) }.getOrNull()

        // Fast path: if remote preferences explicitly responded with bundles, and no enabled bundle targets this app, skip
        if (remoteBundles != null && remoteBundles.isNotEmpty()) {
            val matching = remoteBundles.filter { it.enabled && param.packageName in it.apps }
            if (matching.isEmpty()) return
        }

        targetPackageName = param.packageName

        inContext(param) { app ->
            if (isHookApplied) return@inContext
            isHookApplied = true

            // Initialize in-module XposedBridge with this XposedInterface instance
            XposedBridge.init(this)

            // Install stealth cloak to hide NexFusion from target app package queries & Play Services
            io.github.nexfusion.core.StealthCloak.install(app, moduleApplicationInfo.packageName, this)

            if (isPatchedApp(param)) {
                Toast.makeText(app, "NexFusion does not work with patched apps", Toast.LENGTH_LONG).show()
                return@inContext
            }

            // Fallback / persistent bundle cache for idle, doze, or binder disconnect resilience
            val localPrefs = app.getSharedPreferences("nexfusion_bundles", Application.MODE_PRIVATE)
            val dpsPrefs = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                runCatching { app.createDeviceProtectedStorageContext()?.getSharedPreferences("nexfusion_bundles", Application.MODE_PRIVATE) }.getOrNull()
            } else null

            val bundles = if (remoteBundles != null && remoteBundles.isNotEmpty()) {
                runCatching { BundleRegistry.write(localPrefs, remoteBundles) }
                dpsPrefs?.let { runCatching { BundleRegistry.write(it, remoteBundles) } }
                remoteBundles
            } else {
                val cached = BundleRegistry.read(localPrefs).ifEmpty {
                    dpsPrefs?.let { BundleRegistry.read(it) } ?: emptyList()
                }
                if (cached.isNotEmpty()) {
                    PatcherLog.info { "NexFusion: using locally cached bundles for ${param.packageName} (offline/idle fallback)" }
                    cached
                } else {
                    remoteBundles ?: emptyList()
                }
            }

            val matchingBundles = bundles.filter { it.enabled && param.packageName in it.apps }
            if (matchingBundles.isEmpty()) return@inContext

            resourceMappings = object : ResourceFinder {
                override operator fun get(type: String, name: String): Int {
                    val id = app.resources.getIdentifier(name, type, app.packageName)
                    if (id == 0) throw Exception("Could not find resource type: $type name: $name")
                    return id
                }
            }
            PatcherLog.verbose = BuildConfig.DEBUG

            val session = PatchSession(app, param, this, BuildConfig.COMMIT_HASH, BuildConfig.DEBUG)
            val loaded = matchingBundles.mapNotNull { bundle ->
                runCatching { loadBundle(app, bundle, matchingBundles) }
                    .onFailure { session.reportBundleFailure(bundle.name, it) }
                    .getOrNull()
            }
            // Register every bundle's resources before any patch adds them to a context.
            loaded.forEach { (info, _) -> BundleResources.register(info.apkPath) }
            // A bundle that throws while applying must not abort the others, and the session must
            // always finish: finish() persists the fingerprint cache, and without that every start
            // of the app would re-run every finder and toast again.
            loaded.forEach { (info, patches) ->
                runCatching { session.apply(info, patches) }
                    .onFailure { session.reportBundleFailure(info.name, it) }
            }
            session.finish()
        }
    }

    /**
     * Copies the bundle APK from the module's remote files into the app's persistent internal storage and loads it.
     *
     * Stored in [Application.getFilesDir] so the bundle survives Android OS idle cache trimming, low-memory
     * evictions, and deep doze cycles. The copy is named after the remote file, which changes with every bundle update.
     */
    private fun loadBundle(
        app: Application,
        bundle: InstalledBundle,
        activeBundles: List<InstalledBundle>,
    ): Pair<BundleInfo, Array<Patch>> {
        val dir = File(app.filesDir, "nexfusion").apply { mkdirs() }

        // Clean up legacy codeCacheDir if present to reclaim storage
        runCatching {
            val legacyDir = File(app.codeCacheDir, "nexfusion")
            if (legacyDir.exists()) legacyDir.deleteRecursively()
        }

        // Only prune files if activeBundles is not empty to avoid wiping cached bundles on IPC disconnect
        if (activeBundles.isNotEmpty()) {
            dir.listFiles()?.forEach { file ->
                // Bundle APKs and the dex files remapped from them start with the bundle's file name.
                if (activeBundles.none { file.name.startsWith(it.fileName) } || file.name.endsWith(".tmp")) file.delete()
            }
        }

        val apk = File(dir, bundle.fileName)
        val dpsDir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            runCatching { File(app.createDeviceProtectedStorageContext()?.filesDir, "nexfusion").apply { mkdirs() } }.getOrNull()
        } else null
        val dpsApk = dpsDir?.let { File(it, bundle.fileName) }

        if (!apk.exists() || apk.length() == 0L) {
            if (apk.exists()) apk.delete()
            if (dpsApk != null && dpsApk.exists() && dpsApk.length() > 0L) {
                runCatching { dpsApk.copyTo(apk, overwrite = true) }
            }
            if (!apk.exists() || apk.length() == 0L) {
                val tmp = File(dir, "${bundle.fileName}.tmp")
                openRemoteFile(bundle.fileName).use { pfd ->
                    FileInputStream(pfd.fileDescriptor).use { input ->
                        tmp.outputStream().use { input.copyTo(it) }
                    }
                }
                if (!tmp.renameTo(apk)) throw IllegalStateException("Cannot store bundle ${bundle.id}")
            }
        }
        if (dpsApk != null && (!dpsApk.exists() || dpsApk.length() != apk.length())) {
            runCatching { apk.copyTo(dpsApk, overwrite = true) }
        }

        val manifest = BundleManifest.read(app.packageManager, apk)
        val (classLoader, apps) = BundleClassLoader.load(
            apk, manifest.entryClass, bundleParentClassLoader, remappedDex(dir, apk)
        )
        val patches = apps.filter { it.packageName == app.packageName }
            .flatMap { it.patches.asList() }.toTypedArray()
        return BundleInfo(manifest.id, manifest.name, manifest.version, apk.path, classLoader) to patches
    }

    /**
     * The framework may rename the Xposed API, see [XposedDexRemapper]. Detected from this module's own code,
     * which the framework rewrote. The original names are assembled at runtime so the framework cannot rewrite them.
     */
    private val remapper by lazy {
        fun slash(vararg parts: String) = "L" + parts.joinToString("/") + "/"
        fun classDesc(vararg parts: String) = "L" + parts.joinToString("/") + ";"
        val extraMappings = mapOf(
            slash("de", "robv", "android", "xposed") to "Lio/github/nexfusion/compat/",
            classDesc("android", "app", "AndroidAppHelper") to "Lio/github/nexfusion/compat/AndroidAppHelper;",
        )
        fun dot(vararg parts: String) = parts.joinToString(".")
        XposedDexRemapper.detect(
            XposedInterface::class.java to dot("io", "github", "libxposed", "api", "XposedInterface"),
            extraMappings = extraMappings,
        )
    }

    private val bundleParentClassLoader by lazy {
        BundleParentClassLoader(
            MainHook::class.java.classLoader!!,
            listOf(XposedBridge::class.java, XposedInterface::class.java).map { it.name.substringBeforeLast('.') + "." } +
                remapper.renamedClassPrefixes,
        )
    }

    /** The bundle's classes using the renamed Xposed API, rewritten to match it, or null if none need it. */
    private fun remappedDex(dir: File, apk: File): File? {
        if (!remapper.isNeeded) return null
        val dex = File(dir, "${apk.name}-${remapper.key}.dex")
        // Marks bundles without classes to remap, so they are not scanned again.
        val none = File(dir, "${apk.name}-${remapper.key}.none")
        if (dex.exists()) return dex
        if (none.exists()) return null
        val time = System.currentTimeMillis()
        val remapped = remapper.remap(apk, dex)
        PatcherLog.info { "Remapped the Xposed API in ${apk.name} in ${System.currentTimeMillis() - time}ms" }
        if (!remapped) none.createNewFile()
        return dex.takeIf { remapped }
    }

    /** Tells NexFusion's own app that the module is active, see [ModuleStatus]. */
    private fun reportActive(classLoader: ClassLoader) {
        val status = classLoader.loadClass(ModuleStatus::class.java.name)
        val framework = "$frameworkName $frameworkVersion"
        hook(status.getMethod("isActive")).intercept { true }
        hook(status.getMethod("frameworkName")).intercept { framework }
    }

    private fun isPatchedApp(param: PackageReadyParam): Boolean = listOf(
        "app.morphe.extension.shared.Utils",
        "app.morphe.extension.shared.utils.Utils",
        "app.revanced.integrations.shared.Utils",
        "app.revanced.integrations.shared.utils.Utils",
        "app.revanced.extension.shared.Utils",
        // Patched with Spotify patches.
        "app.twoeno.extension.shared.Logger",
    ).any { runCatching { param.classLoader.loadClass(it) }.isSuccess }
}

context(xposed: XposedInterface)
fun inContext(lpparam: PackageReadyParam, f: (Application) -> Unit) {
    val appClassName = lpparam.applicationInfo.className ?: Application::class.java.name
    val appClazz = Class.forName(appClassName, false, lpparam.classLoader)
    xposed.hook(appClazz.getMethod("onCreate")).intercept { chain ->
        f(chain.thisObject as Application)
        chain.proceed()
    }
}
