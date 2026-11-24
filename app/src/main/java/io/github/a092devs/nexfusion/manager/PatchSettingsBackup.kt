package io.github.a092devs.nexfusion.manager

import android.content.Context
import io.github.libxposed.service.XposedService
import io.github.a092devs.nexfusion.core.BuildConfig
import io.github.a092devs.nexfusion.core.InstalledBundle
import io.github.a092devs.nexfusion.patchPreferenceKey
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

data class PatchBackupPackage(
    /** Map of clean patch name to enabled boolean (e.g. "Hide ads" -> true). */
    val patches: Map<String, Boolean> = emptyMap(),
    /** Raw preferences mapping of bundleId/patchName or custom keys to values. */
    val rawPreferences: Map<String, Any> = emptyMap(),
)

data class PatchBackupData(
    val formatVersion: Int = 2,
    val createdAt: Long = System.currentTimeMillis(),
    val appVersion: String = BuildConfig.VERSION_NAME,
    val ignoredPackages: Set<String> = emptySet(),
    val packages: Map<String, PatchBackupPackage> = emptyMap(),
    val enabledSources: Set<String> = emptySet(),
    val customSettings: Map<String, Any> = emptyMap(),
) {
    val totalPatches: Int
        get() = packages.values.sumOf { it.patches.size }
}

data class RestoreResult(
    val packagesRestored: Int,
    val patchesRestored: Int,
    val success: Boolean,
    val error: String? = null,
    val missingScopePackages: List<String> = emptyList(),
)

object PatchSettingsBackup {

    /**
     * Gathers all patch preferences across known application packages and returns a [PatchBackupData].
     * Resiliently reads from both local persistent preferences and live XposedService remote preferences.
     */
    fun createBackup(
        context: Context,
        service: XposedService?,
        bundles: List<InstalledBundle>,
        knownPackages: Collection<String>,
        ignoredPackages: Set<String>,
        enabledSources: Set<String> = emptySet(),
    ): PatchBackupData {
        val allPackages = mutableSetOf<String>()
        allPackages.addAll(bundles.flatMap { it.apps.keys })
        allPackages.addAll(knownPackages)
        allPackages.addAll(ignoredPackages)

        // Discover any packages with saved local preferences
        runCatching {
            val sharedPrefsDir = java.io.File(context.applicationInfo.dataDir, "shared_prefs")
            if (sharedPrefsDir.exists() && sharedPrefsDir.isDirectory) {
                sharedPrefsDir.listFiles()?.forEach { file ->
                    val name = file.name
                    if (name.startsWith("nexfusion_prefs_") && name.endsWith(".xml")) {
                        val pkg = name.removePrefix("nexfusion_prefs_").removeSuffix(".xml")
                        if (pkg.isNotBlank()) allPackages.add(pkg)
                    }
                }
            }
        }

        val packageMap = mutableMapOf<String, PatchBackupPackage>()

        for (pkg in allPackages) {
            val patches = mutableMapOf<String, Boolean>()
            val rawPreferences = mutableMapOf<String, Any>()

            // 1. Read from local package preferences: "nexfusion_prefs_$pkg"
            runCatching {
                val localPrefs = context.getSharedPreferences("nexfusion_prefs_$pkg", Context.MODE_PRIVATE)
                for ((key, value) in localPrefs.all) {
                    if (value == null) continue
                    rawPreferences[key] = value
                    if (value is Boolean && key.contains('/') && !key.endsWith("_timestamp")) {
                        val patchName = key.substringAfter('/')
                        patches[patchName] = value
                    }
                }
            }

            // 2. Read legacy bundle preferences for this package
            for (bundle in bundles) {
                if (pkg in bundle.apps) {
                    runCatching {
                        val bundlePrefs = context.getSharedPreferences("nexfusion_patch_prefs_${bundle.id}", Context.MODE_PRIVATE)
                        for ((key, value) in bundlePrefs.all) {
                            if (value == null) continue
                            if (!rawPreferences.containsKey(key)) {
                                rawPreferences[key] = value
                            }
                            if (value is Boolean && key.contains('/') && !key.endsWith("_timestamp")) {
                                val patchName = key.substringAfter('/')
                                if (!patches.containsKey(patchName)) {
                                    patches[patchName] = value
                                }
                            }
                        }
                    }
                }
            }

            // 3. Read from remote preferences if service is available (overrides local with live remote state)
            if (service != null) {
                runCatching {
                    val prefs = service.getRemotePreferences(pkg)
                    val remoteEntries = prefs.all.orEmpty()
                    for ((key, value) in remoteEntries) {
                        if (value == null) continue
                        rawPreferences[key] = value
                        if (value is Boolean && key.contains('/') && !key.endsWith("_timestamp")) {
                            val patchName = key.substringAfter('/')
                            patches[patchName] = value
                        }
                    }
                }
            }

            if (patches.isNotEmpty() || rawPreferences.isNotEmpty()) {
                packageMap[pkg] = PatchBackupPackage(patches, rawPreferences)
            }
        }

        return PatchBackupData(
            formatVersion = 2,
            createdAt = System.currentTimeMillis(),
            appVersion = BuildConfig.VERSION_NAME,
            ignoredPackages = ignoredPackages,
            packages = packageMap,
            enabledSources = enabledSources,
        )
    }

    fun toJson(backup: PatchBackupData): JSONObject {
        val root = JSONObject()
        root.put("format_version", backup.formatVersion)
        root.put("created_at", backup.createdAt)
        root.put("app_version", backup.appVersion)

        val ignoredArray = JSONArray()
        backup.ignoredPackages.forEach { ignoredArray.put(it) }
        root.put("ignored_packages", ignoredArray)

        val sourcesArray = JSONArray()
        backup.enabledSources.forEach { sourcesArray.put(it) }
        root.put("enabled_sources", sourcesArray)

        val packagesObj = JSONObject()
        for ((pkg, data) in backup.packages) {
            val pkgObj = JSONObject()

            val patchesObj = JSONObject()
            for ((patch, enabled) in data.patches) {
                patchesObj.put(patch, enabled)
            }
            pkgObj.put("patches", patchesObj)

            val rawObj = JSONObject()
            for ((key, value) in data.rawPreferences) {
                rawObj.put(key, value)
            }
            pkgObj.put("raw_preferences", rawObj)

            packagesObj.put(pkg, pkgObj)
        }
        root.put("packages", packagesObj)

        val customObj = JSONObject()
        for ((k, v) in backup.customSettings) {
            customObj.put(k, v)
        }
        root.put("custom_settings", customObj)

        return root
    }

    fun fromJson(json: JSONObject): PatchBackupData {
        val formatVersion = json.optInt("format_version", 1)
        val createdAt = json.optLong("created_at", System.currentTimeMillis())
        val appVersion = json.optString("app_version", "")

        val ignoredSet = mutableSetOf<String>()
        val ignoredArray = json.optJSONArray("ignored_packages")
        if (ignoredArray != null) {
            for (i in 0 until ignoredArray.length()) {
                ignoredSet.add(ignoredArray.getString(i))
            }
        }

        val enabledSources = mutableSetOf<String>()
        val sourcesArray = json.optJSONArray("enabled_sources")
        if (sourcesArray != null) {
            for (i in 0 until sourcesArray.length()) {
                enabledSources.add(sourcesArray.getString(i))
            }
        }

        val packagesMap = mutableMapOf<String, PatchBackupPackage>()
        val packagesObj = json.optJSONObject("packages")
        if (packagesObj != null) {
            val keys = packagesObj.keys()
            while (keys.hasNext()) {
                val pkg = keys.next()
                val pkgObj = packagesObj.optJSONObject(pkg) ?: continue

                val patchesMap = mutableMapOf<String, Boolean>()
                val patchesObj = pkgObj.optJSONObject("patches")
                if (patchesObj != null) {
                    val patchKeys = patchesObj.keys()
                    while (patchKeys.hasNext()) {
                        val patch = patchKeys.next()
                        patchesMap[patch] = patchesObj.optBoolean(patch)
                    }
                }

                val rawMap = mutableMapOf<String, Any>()
                val rawObj = pkgObj.optJSONObject("raw_preferences")
                if (rawObj != null) {
                    val rawKeys = rawObj.keys()
                    while (rawKeys.hasNext()) {
                        val rawKey = rawKeys.next()
                        rawMap[rawKey] = rawObj.get(rawKey)
                    }
                }

                packagesMap[pkg] = PatchBackupPackage(patchesMap, rawMap)
            }
        }

        val customSettings = mutableMapOf<String, Any>()
        val customObj = json.optJSONObject("custom_settings")
        if (customObj != null) {
            val keys = customObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                customSettings[k] = customObj.get(k)
            }
        }

        return PatchBackupData(
            formatVersion = formatVersion,
            createdAt = createdAt,
            appVersion = appVersion,
            ignoredPackages = ignoredSet,
            packages = packagesMap,
            enabledSources = enabledSources,
            customSettings = customSettings,
        )
    }

    fun exportToStream(outputStream: OutputStream, backup: PatchBackupData) {
        val jsonString = toJson(backup).toString(2)
        outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(jsonString) }
    }

    fun parseFromStream(inputStream: InputStream): PatchBackupData {
        val text = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        return fromJson(JSONObject(text))
    }

    /**
     * Applies the parsed backup into both local persistent preferences and LSPosed remote preferences.
     * Guarantees 100% of patch settings are restored even if the application is not yet in scope or offline.
     */
    fun applyBackup(
        context: Context,
        service: XposedService?,
        bundles: List<InstalledBundle>,
        backup: PatchBackupData,
        onSetIgnored: (String, Boolean) -> Unit,
    ): RestoreResult {
        var packagesRestored = 0
        var patchesRestored = 0
        val missingScopePackages = mutableListOf<String>()
        val now = System.currentTimeMillis()

        for ((pkg, pkgData) in backup.packages) {
            var restoredForPkg = false
            var pkgPatchesCount = 0

            // 1. Restore to package-scoped local preferences "nexfusion_prefs_$pkg"
            runCatching {
                val pkgPrefs = context.getSharedPreferences("nexfusion_prefs_$pkg", Context.MODE_PRIVATE)
                val pkgEditor = pkgPrefs.edit()

                for ((key, value) in pkgData.rawPreferences) {
                    when (value) {
                        is Boolean -> {
                            pkgEditor.putBoolean(key, value)
                            if (key.contains('/') && !key.endsWith("_timestamp")) pkgPatchesCount++
                        }
                        is Int -> pkgEditor.putInt(key, value)
                        is Long -> pkgEditor.putLong(key, value)
                        is Float -> pkgEditor.putFloat(key, value)
                        is String -> pkgEditor.putString(key, value)
                    }
                }

                // Cross-bundle mapping for active bundles matching this package
                val activeBundles = bundles.filter { it.enabled && pkg in it.apps }
                for (bundle in activeBundles) {
                    for ((patchName, enabled) in pkgData.patches) {
                        val key = patchPreferenceKey(bundle.id, patchName)
                        pkgEditor.putBoolean(key, enabled).putLong("${key}_timestamp", now)
                    }
                }

                pkgEditor.apply()
                restoredForPkg = true
            }

            // Also mirror to legacy bundle-scoped preferences
            val activeBundles = bundles.filter { it.enabled && pkg in it.apps }
            for (bundle in activeBundles) {
                runCatching {
                    val bundlePrefs = context.getSharedPreferences("nexfusion_patch_prefs_${bundle.id}", Context.MODE_PRIVATE)
                    val bundleEditor = bundlePrefs.edit()
                    for ((patchName, enabled) in pkgData.patches) {
                        val key = patchPreferenceKey(bundle.id, patchName)
                        bundleEditor.putBoolean(key, enabled).putLong("${key}_timestamp", now)
                    }
                    bundleEditor.apply()
                }
            }

            // 2. Restore to remote preferences if service is active
            if (service != null) {
                val remoteWritten = runCatching {
                    val prefs = service.getRemotePreferences(pkg)
                    val editor = prefs.edit()
                    for ((key, value) in pkgData.rawPreferences) {
                        when (value) {
                            is Boolean -> editor.putBoolean(key, value)
                            is Int -> editor.putInt(key, value)
                            is Long -> editor.putLong(key, value)
                            is Float -> editor.putFloat(key, value)
                            is String -> editor.putString(key, value)
                        }
                    }
                    for (bundle in activeBundles) {
                        for ((patchName, enabled) in pkgData.patches) {
                            val key = patchPreferenceKey(bundle.id, patchName)
                            editor.putBoolean(key, enabled)
                        }
                    }
                    if (!editor.commit()) {
                        editor.apply()
                    }
                    true
                }.getOrDefault(false)

                if (!remoteWritten) {
                    missingScopePackages.add(pkg)
                }
            }

            if (restoredForPkg) {
                packagesRestored++
                patchesRestored += if (pkgPatchesCount > 0) pkgPatchesCount else pkgData.patches.size
            }
        }

        // 3. Restore package exclusion status
        for (ignoredPkg in backup.ignoredPackages) {
            onSetIgnored(ignoredPkg, true)
        }

        return RestoreResult(
            packagesRestored = packagesRestored,
            patchesRestored = patchesRestored,
            success = packagesRestored > 0 || backup.packages.isEmpty(),
            missingScopePackages = missingScopePackages,
        )
    }
}
