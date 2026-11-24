package io.github.a092devs.nexfusion.manager

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
    val patches: Map<String, Boolean>,
    /** Raw preferences mapping of bundleId/patchName or custom keys to values. */
    val rawPreferences: Map<String, Any>,
)

data class PatchBackupData(
    val formatVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val appVersion: String = BuildConfig.VERSION_NAME,
    val ignoredPackages: Set<String> = emptySet(),
    val packages: Map<String, PatchBackupPackage> = emptyMap(),
) {
    val totalPatches: Int
        get() = packages.values.sumOf { it.patches.size }
}

data class RestoreResult(
    val packagesRestored: Int,
    val patchesRestored: Int,
    val success: Boolean,
    val error: String? = null,
)

object PatchSettingsBackup {

    /**
     * Gathers all patch preferences across known application packages and returns a [PatchBackupData].
     */
    fun createBackup(
        service: XposedService?,
        bundles: List<InstalledBundle>,
        knownPackages: Collection<String>,
        ignoredPackages: Set<String>,
    ): PatchBackupData {
        val allPackages = (bundles.flatMap { it.apps.keys } + knownPackages).distinct()
        val packageMap = mutableMapOf<String, PatchBackupPackage>()

        if (service != null) {
            for (pkg in allPackages) {
                val prefs = runCatching { service.getRemotePreferences(pkg) }.getOrNull() ?: continue
                val allEntries = runCatching { prefs.all }.getOrNull().orEmpty()
                if (allEntries.isEmpty()) continue

                val patches = mutableMapOf<String, Boolean>()
                val rawPreferences = mutableMapOf<String, Any>()

                for ((key, value) in allEntries) {
                    if (value == null) continue
                    rawPreferences[key] = value

                    // Keys are formatted as "$bundleId/$patchName"
                    if (value is Boolean && key.contains('/')) {
                        val patchName = key.substringAfter('/')
                        patches[patchName] = value
                    }
                }

                if (patches.isNotEmpty() || rawPreferences.isNotEmpty()) {
                    packageMap[pkg] = PatchBackupPackage(patches, rawPreferences)
                }
            }
        }

        return PatchBackupData(
            formatVersion = 1,
            createdAt = System.currentTimeMillis(),
            appVersion = BuildConfig.VERSION_NAME,
            ignoredPackages = ignoredPackages,
            packages = packageMap,
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

        return PatchBackupData(
            formatVersion = formatVersion,
            createdAt = createdAt,
            appVersion = appVersion,
            ignoredPackages = ignoredSet,
            packages = packagesMap,
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
     * Applies the parsed backup into LSPosed remote preferences.
     * Intelligently combines raw preferences with active installed bundle key re-mapping.
     */
    fun applyBackup(
        backup: PatchBackupData,
        service: XposedService?,
        bundles: List<InstalledBundle>,
        onSetIgnored: (String, Boolean) -> Unit,
    ): RestoreResult {
        if (service == null) {
            return RestoreResult(0, 0, false, "LSPosed framework service is currently inactive")
        }

        var packagesRestored = 0
        var patchesRestored = 0

        for ((pkg, pkgData) in backup.packages) {
            val prefs = runCatching { service.getRemotePreferences(pkg) }.getOrNull() ?: continue
            val editor = prefs.edit()
            var pkgPatchesCount = 0

            // 1. Write raw preferences
            for ((key, value) in pkgData.rawPreferences) {
                when (value) {
                    is Boolean -> {
                        editor.putBoolean(key, value)
                        if (key.contains('/')) pkgPatchesCount++
                    }
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is String -> editor.putString(key, value)
                }
            }

            // 2. Intelligent cross-bundle remapping:
            // If the user's active bundle IDs differ from the backup's raw bundle IDs,
            // map patchName -> activeBundle.id so preferences don't get lost.
            val activeBundles = bundles.filter { it.enabled && pkg in it.apps }
            for (bundle in activeBundles) {
                for ((patchName, enabled) in pkgData.patches) {
                    val key = patchPreferenceKey(bundle.id, patchName)
                    editor.putBoolean(key, enabled)
                }
            }

            val committed = editor.commit()
            if (!committed) {
                editor.apply()
            }

            packagesRestored++
            patchesRestored += if (pkgPatchesCount > 0) pkgPatchesCount else pkgData.patches.size
        }

        // 3. Restore package exclusion status
        for (ignoredPkg in backup.ignoredPackages) {
            onSetIgnored(ignoredPkg, true)
        }

        return RestoreResult(packagesRestored, patchesRestored, true)
    }
}
