@file:OptIn(DexKitExperimentalApi::class)

package io.github.a092devs.nexfusion

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import io.github.a092devs.nexfusion.compat.XposedBridge
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface
import io.github.a092devs.nexfusion.core.PrefKeys
import org.luckypray.dexkit.DexKitCacheBridge
import org.luckypray.dexkit.annotations.DexKitExperimentalApi
import kotlin.system.measureTimeMillis

@Suppress("UNCHECKED_CAST")
class SharedPrefCache(app: Application) : DexKitCacheBridge.Cache {
    val pref = app.getSharedPreferences(PrefKeys.DEXKIT_CACHE, MODE_PRIVATE)!!
    private val dpsPref = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        runCatching { app.createDeviceProtectedStorageContext()?.getSharedPreferences(PrefKeys.DEXKIT_CACHE, MODE_PRIVATE) }.getOrNull()
    } else null

    private var dirty = false

    private val map = mutableMapOf<String, String>().apply {
        putAll(pref.all as Map<String, String>)
        if (isEmpty() && dpsPref != null) {
            putAll(dpsPref.all as Map<String, String>)
        }
    }

    override fun clearAll() {
        if (map.isNotEmpty()) {
            map.clear()
            dirty = true
        }
    }

    override fun getString(key: String, default: String?): String? = map.getOrDefault(key, default)

    override fun getAllKeys(): Collection<String> = map.keys

    override fun getStringList(
        key: String, default: List<String>?
    ): List<String>? =
        map.getOrDefault(key, null)?.takeIf(String::isNotBlank)?.split('|') ?: default

    override fun putString(key: String, value: String) {
        if (map.put(key, value) != value) {
            dirty = true
        }
    }

    override fun putStringList(key: String, value: List<String>) {
        val joined = value.joinToString("|")
        if (map.put(key, joined) != joined) {
            dirty = true
        }
    }

    override fun remove(key: String) {
        if (map.remove(key) != null) {
            dirty = true
        }
    }

    fun saveCache() {
        if (!dirty) return
        dirty = false

        val edit = pref.edit()
        edit.clear()
        map.forEach { (k, v) ->
            edit.putString(k, v)
        }
        edit.apply()

        dpsPref?.let { dps ->
            runCatching {
                val dpsEdit = dps.edit()
                dpsEdit.clear()
                map.forEach { (k, v) -> dpsEdit.putString(k, v) }
                dpsEdit.apply()
            }
        }
    }
}

/**
 * Applies the patches of every loaded bundle to one app process.
 *
 * Fingerprint matches are cached per bundle and invalidated whenever the app, NexFusion or the bundle changes.
 */
open class PatchSession(
    private val app: Application,
    private val lpparam: XposedModuleInterface.PackageReadyParam,
    private val xposed: XposedInterface,
    /** Identifies the NexFusion build; part of every cache key. */
    private val coreVersion: String,
    /** Enables the debug-only success log. */
    private val debug: Boolean,
) {
    private val cache = SharedPrefCache(app)
    private val failedPatches = mutableListOf<String>()
    private val appVersion by lazy { readAppVersion() }
    private val sessionAppliedPatches = mutableSetOf<String>()

    init {
        XposedBridge.init(xposed)
        System.loadLibrary("dexkit")
        DexKitCacheBridge.init(cache)
    }

    fun apply(bundle: BundleInfo, patches: Array<Patch>) {
        val time = measureTimeMillis {
            loadCacheIfValid(bundle)
            val executor = PatchExecutor(
                app, lpparam, xposed, bundle,
                DexKitCacheBridge.create(bundle.id, lpparam.applicationInfo.sourceDir),
                sessionAppliedPatches = sessionAppliedPatches
            )
            try {
                executor.executePatches(patches)
            } finally {
                executor.closeDexKit()
            }
            executor.failedPatches.mapTo(failedPatches) { "${it.name} (${bundle.name})" }
        }
        PatcherLog.state { "${lpparam.packageName}: applied ${bundle.id} in ${time}ms" }
    }

    /** Records a bundle that could not be loaded at all. */
    fun reportBundleFailure(bundleName: String, error: Throwable) {
        XposedBridge.log(error)
        PatcherLog.error(error) { "Failed to load bundle $bundleName: ${error.message ?: error.javaClass.simpleName}" }
        val msg = error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
        failedPatches.add("all patches ($bundleName: $msg)")
    }

    fun finish() {
        val signature = failedPatches.joinToString()
        if (signature.isNotEmpty()) {
            PatcherLog.error { "${lpparam.packageName} version: $appVersion: failing patches: $signature" }
            // A deterministic miss would toast on every start of the affected app until the system
            // blocks the app's toasts outright (and eats the message); only a change in the failing
            // set is news, the log line above carries the rest.
            val previous = cache.getString(PrefKeys.FAILED_PATCHES, null)
            if (signature != previous) {
                cache.putString(PrefKeys.FAILED_PATCHES, signature)
                showToast("Error while applying the following patches:\n$signature")
            }
        } else {
            if (cache.getString(PrefKeys.FAILED_PATCHES, null)?.isNotEmpty() == true) {
                cache.putString(PrefKeys.FAILED_PATCHES, "")
            }
            if (debug) {
                XposedBridge.log("${lpparam.packageName} version: $appVersion: patches applied successfully")
            }
        }
        cache.saveCache()
    }

    private fun loadCacheIfValid(bundle: BundleInfo) {
        val packageInfo = app.packageManager.getPackageInfo(app.packageName, 0)
        val idKey = PrefKeys.bundleCacheId(bundle.id)
        val appTimeKey = "${idKey}_app_time"
        val bundleHash = java.io.File(bundle.apkPath).nameWithoutExtension
        val appTime = packageInfo.lastUpdateTime.toString()
        val id = "$appTime-$coreVersion-${bundle.version}-$bundleHash"
        val cachedId = cache.getString(idKey, null)
        val cachedAppTime = cache.getString(appTimeKey, null)
        val isCached = cachedId == id
        val appChanged = cachedAppTime != appTime

        PatcherLog.state { "${bundle.id} cache ID: $id, cached ID: $cachedId, using cache: $isCached" }

        if (!isCached) {
            // Only wipe DexKit's resolved method/class cache when the target app APK itself changed.
            // When only the bundle updates, keeping existing DexKit entries avoids a full DEX rescan.
            if (appChanged) {
                DexKitCacheBridge.clearCache(bundle.id)
                cache.putString(appTimeKey, appTime)
                showToast("NexFusion is initializing ${bundle.name}, please wait...")
            }
            cache.putString(idKey, id)
        }
    }

    private fun readAppVersion(): String {
        val packageInfo = app.packageManager.getPackageInfo(app.packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION") packageInfo.versionCode.toLong()
        }
        return "${packageInfo.versionName} ($versionCode)"
    }

    private fun isMainProcess(): Boolean {
        val currentProcess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else {
            runCatching {
                val am = app.getSystemService(android.content.Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
                val pid = android.os.Process.myPid()
                am?.runningAppProcesses?.firstOrNull { it.pid == pid }?.processName
            }.getOrNull() ?: app.packageName
        }
        return currentProcess == app.packageName
    }

    private fun showToast(message: String) {
        if (!isMainProcess()) return
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(app, message, Toast.LENGTH_LONG).show()
        }
    }
}
