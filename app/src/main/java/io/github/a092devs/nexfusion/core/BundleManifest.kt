package io.github.a092devs.nexfusion.core

import android.content.Context
import android.content.pm.PackageInfo
import dalvik.system.DexClassLoader
import android.content.pm.PackageManager
import android.os.Build
import io.github.a092devs.nexfusion.AppPatchInfo
import io.github.a092devs.nexfusion.AppTarget
import io.github.a092devs.nexfusion.PATCHER_API_VERSION
import io.github.a092devs.nexfusion.Patch
import io.github.a092devs.nexfusion.PatchBundle
import io.github.a092devs.nexfusion.PatchExecutor
import java.io.File

import io.github.a092devs.nexfusion.core.compatibility.VersionComparator

/**
 * Metadata of a bundle APK, read from its AndroidManifest without loading any code.
 *
 * Bundles declare it as `<meta-data>` of their `<application>`, see the bundle template.
 */
class BundleManifest(
    val id: String,
    val name: String,
    val version: String,
    val versionCode: Long,
    val entryClass: String,
    val apiVersion: Int,
    val author: String,
    val description: String,
    /** Package id of the bundle's resources, 0 if unknown. */
    val resourcePackageId: Int,
    /** Compatibility series (e.g. "3.2"). */
    val compatibilitySeries: String = "",
    /** Minimum compatible NexFusion manager version (e.g. "3.2.0"). */
    val minManagerVersion: String = "",
    /** Maximum compatible NexFusion manager version. */
    val maxManagerVersion: String = "",
) {
    /**
     * Checks whether this bundle recommends a newer manager version.
     * Returns an advisory warning message if an update is recommended, or null if fully aligned.
     * Never aborts installation or blocks cross-version usage.
     */
    fun checkCompatibilityWarning(managerVersion: String = BuildConfig.VERSION_NAME): String? {
        val cleanManager = managerVersion.trim().removePrefix("v").removePrefix("V").trim()
        val managerSeries = cleanManager.split('.').take(2).joinToString(".")

        if (compatibilitySeries.isNotBlank()) {
            val cleanSeries = compatibilitySeries.trim().removePrefix("v").removePrefix("V").trim()
            if (cleanSeries != managerSeries) {
                val seriesComparison = VersionComparator.compare(cleanSeries, managerSeries)
                if (seriesComparison > 0) {
                    return "Bundle '$name' $version was built for NexFusion series $compatibilitySeries (current: $managerVersion). An app update is recommended."
                } 
            }
        }

        if (minManagerVersion.isNotBlank()) {
            if (VersionComparator.compare(cleanManager, minManagerVersion) < 0) {
                return "Bundle '$name' $version recommends NexFusion $minManagerVersion or newer (current: $managerVersion). An app update is recommended."
            }
        }

        if (maxManagerVersion.isNotBlank()) {
            if (VersionComparator.compare(cleanManager, maxManagerVersion) > 0) {
                return "Bundle '$name' $version recommends NexFusion $maxManagerVersion or older (current: $managerVersion)."
            }
        }

        return null
    }

    /**
     * Non-blocking compatibility validation. Returns an advisory warning message if the bundle
     * targets a different manager version, or null if fully aligned.
     */
    fun validateCompatibility(managerVersion: String = BuildConfig.VERSION_NAME): String? {
        return checkCompatibilityWarning(managerVersion)
    }

    companion object {
        const val META_ENTRY = "nexfusion.bundle.entry"
        const val META_API_VERSION = "nexfusion.bundle.apiVersion"
        const val META_AUTHOR = "nexfusion.bundle.author"
        const val META_DESCRIPTION = "nexfusion.bundle.description"
        const val META_COMPATIBILITY_SERIES = "nexfusion.bundle.compatibilitySeries"
        const val META_MIN_MANAGER_VERSION = "nexfusion.bundle.minManagerVersion"
        const val META_MAX_MANAGER_VERSION = "nexfusion.bundle.maxManagerVersion"

        /** @throws InvalidBundleException if [apk] is not a usable patch bundle. */
        fun read(packageManager: PackageManager, apk: File): BundleManifest {
            if (!apk.exists() || apk.length() == 0L) {
                throw InvalidBundleException("Empty or missing bundle file: ${apk.name}")
            }
            @Suppress("DEPRECATION")
            val info: PackageInfo = packageManager.getPackageArchiveInfo(
                apk.path, PackageManager.GET_META_DATA
            ) ?: throw InvalidBundleException("Not an APK file: ${apk.name}")
            val app = info.applicationInfo ?: throw InvalidBundleException("APK has no application")
            app.sourceDir = apk.path
            app.publicSourceDir = apk.path
            val meta = app.metaData ?: throw InvalidBundleException("Not a NexFusion patch bundle")
            val entry = meta.getString(META_ENTRY)
                ?: throw InvalidBundleException("Not a NexFusion patch bundle")
            val apiVersion = meta.getInt(META_API_VERSION, 0)
            if (apiVersion < 1) throw InvalidBundleException("Not a NexFusion patch bundle")
            if (apiVersion > PATCHER_API_VERSION) throw InvalidBundleException(
                "Bundle requires patcher API $apiVersion, but this NexFusion supports up to $PATCHER_API_VERSION. Update NexFusion."
            )
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION") info.versionCode.toLong()
            }
            // A meta-data value referencing a string resource is stored as the resource id.
            fun metaString(key: String): String = when (val value = meta.get(key)) {
                is Int -> runCatching {
                    packageManager.getResourcesForApplication(app).getString(value)
                }.getOrDefault("")
                null -> ""
                else -> value.toString()
            }

            return BundleManifest(
                id = info.packageName,
                name = app.loadLabel(packageManager).toString(),
                version = info.versionName ?: versionCode.toString(),
                versionCode = versionCode,
                entryClass = entry,
                apiVersion = apiVersion,
                author = metaString(META_AUTHOR),
                description = metaString(META_DESCRIPTION),
                resourcePackageId = app.labelRes ushr 24,
                compatibilitySeries = metaString(META_COMPATIBILITY_SERIES),
                minManagerVersion = metaString(META_MIN_MANAGER_VERSION),
                maxManagerVersion = metaString(META_MAX_MANAGER_VERSION),
            )
        }
    }
}

open class InvalidBundleException(message: String, cause: Throwable? = null) : Exception(message, cause)

class IncompatibleBundleException(message: String, cause: Throwable? = null) : InvalidBundleException(message, cause)


/**
 * Gives bundles access to the classes NexFusion shares with them: the patcher API and what it exposes.
 *
 * Everything else comes from the framework, so libraries NexFusion uses internally never shadow a bundle's own copies.
 */
class BundleParentClassLoader(
    private val module: ClassLoader,
    /** Further class name prefixes to share, such as the Xposed API as the framework renamed it. */
    extraSharedPrefixes: Collection<String> = emptyList(),
) : ClassLoader(Context::class.java.classLoader) {
    private val sharedPrefixes = listOf(
        "kotlin.",
        "io.github.a092devs.nexfusion.",
        "io.github.a092devs.",
        "io.github.nexfusion.",
        "org.luckypray.dexkit.",
        "com.google.flatbuffers.",
        "androidx.annotation.",
        listOf("de", "robv", "android", "xposed").joinToString(".") + ".",
        "io.github.libxposed.",
    ) + extraSharedPrefixes

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        if (sharedPrefixes.any { name.startsWith(it) }) {
            try {
                return module.loadClass(name)
            } catch (e: ClassNotFoundException) {
                if (name.startsWith("io.github.nexfusion.")) {
                    val modernName = name.replaceFirst("io.github.nexfusion.", "io.github.a092devs.nexfusion.")
                    try {
                        return module.loadClass(modernName)
                    } catch (_: ClassNotFoundException) {}
                }
                if (name.startsWith("io.github.morphe.")) {
                    val modernName = name.replaceFirst("io.github.morphe.", "io.github.a092devs.nexfusion.")
                    try {
                        return module.loadClass(modernName)
                    } catch (_: ClassNotFoundException) {}
                }
                return super.loadClass(name, resolve)
            }
        }
        return super.loadClass(name, resolve)
    }
}

/** Loads the code of a bundle APK and instantiates its [PatchBundle]. */
object BundleClassLoader {
    /**
     * @param prependedDex Dex file whose classes take precedence over the APK's, see [XposedDexRemapper].
     */
    fun load(
        apk: File,
        entryClass: String,
        parent: ClassLoader,
        prependedDex: File? = null,
    ): Pair<ClassLoader, List<AppPatchInfo>> {
        // Android 14+ refuses to load writable dex files.
        apk.setReadOnly()
        prependedDex?.setReadOnly()
        val path = listOfNotNull(prependedDex, apk).joinToString(File.pathSeparator) { it.path }
        val loader = runCatching { DexClassLoader(path, null, null, parent) }
            .getOrElse { dalvik.system.PathClassLoader(path, parent) }
        android.util.Log.d("NexFusionDebug", "BundleClassLoader.load: apk=$apk entryClass=$entryClass prependedDex=$prependedDex")
        var fallbackApps: List<AppPatchInfo>? = null
        val targetClass = if (prependedDex != null && entryClass.startsWith("io.github.nexfusion.")) {
            entryClass.replaceFirst("io.github.nexfusion.", "io.github.a092devs.nexfusion.")
        } else {
            entryClass
        }
        val entry = try {
            val clazz = try {
                loader.loadClass(targetClass)
            } catch (e: ClassNotFoundException) {
                if (targetClass != entryClass) {
                    loader.loadClass(entryClass)
                } else {
                    val alt = if (entryClass.startsWith("io.github.nexfusion.")) {
                        entryClass.replaceFirst("io.github.nexfusion.", "io.github.a092devs.nexfusion.")
                    } else if (entryClass.startsWith("io.github.a092devs.nexfusion.")) {
                        entryClass.replaceFirst("io.github.a092devs.nexfusion.", "io.github.nexfusion.")
                    } else null
                    if (alt != null) {
                        runCatching { loader.loadClass(alt) }.getOrNull() ?: throw e
                    } else {
                        throw e
                    }
                }
            }
            // A Kotlin object exposes its instance as INSTANCE.
            val obj = runCatching { clazz.getField("INSTANCE").get(null) }.getOrNull()
                ?: clazz.getDeclaredConstructor().newInstance()
            android.util.Log.d("NexFusionDebug", "Instantiated entry=$obj, isPatchBundle=${obj is PatchBundle}")
            obj
        } catch (e: Throwable) {
            android.util.Log.w("NexFusionDebug", "Cannot instantiate bundle entry $entryClass, attempting DEX parsing fallback: ${e.message}", e)
            val extracted = extractAppsFromDex(apk)
            if (extracted.isNotEmpty()) {
                fallbackApps = extracted
                null
            } else {
                android.util.Log.e("NexFusionDebug", "Cannot instantiate bundle entry $entryClass and DEX parsing found no apps", e)
                throw InvalidBundleException("Cannot instantiate bundle entry $entryClass: ${e.message ?: e.javaClass.simpleName}", e)
            }
        }
        val apps: List<AppPatchInfo> = fallbackApps ?: when (entry) {
            is PatchBundle -> entry.apps
            else -> {
                // Compatibility fallback: extract apps via reflection if entry implements legacy or foreign PatchBundle
                val getApps = runCatching { entry?.javaClass?.getMethod("getApps") }.getOrNull()
                if (getApps != null) {
                    val rawList = getApps.invoke(entry) as? List<*>
                    rawList?.mapNotNull { item ->
                        when (item) {
                            is AppPatchInfo -> item
                            null -> null
                            else -> {
                                val appName = runCatching { item.javaClass.getMethod("getAppName").invoke(item) as? String }.getOrNull() ?: ""
                                val packageName = runCatching { item.javaClass.getMethod("getPackageName").invoke(item) as? String }.getOrNull() ?: ""
                                val targets = runCatching { item.javaClass.getMethod("getTargets").invoke(item) as? List<*> }.getOrNull().orEmpty()
                                val iconColor = runCatching { item.javaClass.getMethod("getAppIconColor").invoke(item) as? Int }.getOrNull()
                                val convertedTargets = targets.mapNotNull { t ->
                                    if (t == null) return@mapNotNull null
                                    val v = runCatching { t.javaClass.getMethod("getVersion").invoke(t) as? String }.getOrNull() ?: return@mapNotNull null
                                    val exp = runCatching { t.javaClass.getMethod("getExperimental").invoke(t) as? Boolean }.getOrNull() ?: false
                                    AppTarget(v, exp)
                                }
                                val rawPatches = runCatching {
                                    val getter = item.javaClass.getMethod("getPatches")
                                    when (val res = getter.invoke(item)) {
                                        is Array<*> -> res.filterNotNull()
                                        is Iterable<*> -> res.filterNotNull()
                                        else -> emptyList<Any>()
                                    }
                                }.getOrNull().orEmpty()

                                val convertedPatches = rawPatches.mapNotNull { p ->
                                    if (p is Patch) return@mapNotNull p
                                    val pName = runCatching { p.javaClass.getMethod("getName").invoke(p) as? String }.getOrNull()
                                        ?: runCatching { p.javaClass.getField("name").get(p) as? String }.getOrNull()
                                        ?: return@mapNotNull null
                                    val pDesc = runCatching { p.javaClass.getMethod("getDescription").invoke(p) as? String }.getOrNull()
                                        ?: runCatching { p.javaClass.getField("description").get(p) as? String }.getOrNull()
                                        ?: ""
                                    val pUse = runCatching { p.javaClass.getMethod("getUse").invoke(p) as? Boolean }.getOrNull()
                                        ?: runCatching { p.javaClass.getField("use").get(p) as? Boolean }.getOrNull()
                                        ?: true
                                    val pRun = runCatching {
                                        val m = p.javaClass.getMethod("getRun")
                                        @Suppress("UNCHECKED_CAST")
                                        m.invoke(p) as? (io.github.a092devs.nexfusion.PatchExecutor.() -> Unit)
                                    }.getOrNull() ?: {}
                                    Patch(name = pName, description = pDesc, use = pUse, run = pRun)
                                }.toTypedArray()

                                AppPatchInfo(
                                    appName = appName,
                                    packageName = packageName,
                                    patches = convertedPatches,
                                    targets = convertedTargets,
                                    appIconColor = iconColor,
                                )
                            }
                        }
                    } ?: throw InvalidBundleException("$entryClass does not implement PatchBundle")
                } else {
                    throw InvalidBundleException("$entryClass does not implement PatchBundle")
                }
            }
        }
        android.util.Log.d("NexFusionDebug", "Loaded ${apps.size} apps from bundle: ${apps.map { "${it.packageName}:${it.patches.size}" }}")
        return loader to apps
    }

    private fun extractAppsFromDex(apk: File): List<AppPatchInfo> {
        val opcodes = com.android.tools.smali.dexlib2.Opcodes.getDefault()
        val apps = mutableListOf<AppPatchInfo>()
        val seen = mutableSetOf<String>()
        runCatching {
            java.util.zip.ZipFile(apk).use { zip ->
                zip.entries().asSequence()
                    .filter { it.name.matches(Regex("classes\\d*\\.dex")) }
                    .forEach { entry ->
                        val bytes = zip.getInputStream(entry).use { it.readBytes() }
                        val dex = com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile(opcodes, bytes)
                        for (classDef in dex.classes) {
                            if (classDef.type.contains("NexFusionPatches") || classDef.type.contains("Patches;")) {
                                for (method in classDef.methods) {
                                    val instructions = method.implementation?.instructions ?: continue
                                    var lastString = ""
                                    for (inst in instructions) {
                                        if (inst is com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction) {
                                            val ref = inst.reference
                                            if (ref is com.android.tools.smali.dexlib2.iface.reference.StringReference) {
                                                val str = ref.string
                                                if (str.contains(".") && !str.contains(" ") &&
                                                    (str.startsWith("com.") || str.startsWith("org.") || str.startsWith("app.") || str.startsWith("ch."))
                                                ) {
                                                    val appName = if (lastString.isNotBlank() && !lastString.contains(".")) lastString else str.substringAfterLast('.').replaceFirstChar { it.uppercase() }
                                                    if (seen.add(str)) {
                                                        apps.add(AppPatchInfo(appName = appName, packageName = str, patches = emptyArray()))
                                                    }
                                                }
                                                lastString = str
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }.onFailure {
            android.util.Log.e("NexFusionBundle", "Failed to extract apps from DEX", it)
        }
        return apps
    }
}
