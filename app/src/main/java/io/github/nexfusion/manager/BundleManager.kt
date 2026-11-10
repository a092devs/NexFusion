package io.github.nexfusion.manager

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import io.github.libxposed.service.XposedService
import io.github.nexfusion.AppPatchInfo
import io.github.nexfusion.core.BundleClassLoader
import io.github.nexfusion.core.BundleManifest
import io.github.nexfusion.core.BundleParentClassLoader
import io.github.nexfusion.core.BundleRegistry
import io.github.nexfusion.core.InstalledBundle
import io.github.nexfusion.core.InvalidBundleException
import io.github.nexfusion.core.PrefKeys
import io.github.nexfusion.core.getOrDefault
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

/**
 * Installs, updates and removes patch bundles.
 *
 * Bundle APKs are stored twice: as a remote file of the Xposed service, which hooked apps read,
 * and in the manager's own storage, from which the manager lists their patches.
 *
 * Offline resilience: reads from local preferences when the XposedService is inactive.
 */
class BundleManager(private val context: Context, private val service: XposedService? = null) {
    private val localPrefs get() = context.getSharedPreferences(PrefKeys.BUNDLES_GROUP, Context.MODE_PRIVATE)
    private val registryPrefs get() = service?.takeIf {
        runCatching { it.getRemotePreferences(PrefKeys.BUNDLES_GROUP) }.isSuccess
    }?.getRemotePreferences(PrefKeys.BUNDLES_GROUP)

    private val localDir get() = File(context.filesDir, "bundles").apply { mkdirs() }

    val installed: List<InstalledBundle>
        get() {
            val remote = registryPrefs?.let { runCatching { BundleRegistry.read(it) }.getOrNull() }
            val baseList = if (remote != null) {
                // Sync to local preferences for offline access
                BundleRegistry.write(localPrefs, remote)
                remote
            } else {
                BundleRegistry.read(localPrefs)
            }

            var changed = false
            val refreshed = baseList.map { bundle ->
                val localFile = File(localDir, bundle.fileName)
                if (localFile.exists()) {
                    val freshDesc = runCatching {
                        BundleManifest.read(context.packageManager, localFile).description
                    }.getOrNull()
                    if (!freshDesc.isNullOrEmpty() && freshDesc != bundle.description) {
                        changed = true
                        bundle.copy(description = freshDesc)
                    } else bundle
                } else bundle
            }

            if (changed) {
                registryPrefs?.let { runCatching { BundleRegistry.write(it, refreshed) } }
                BundleRegistry.write(localPrefs, refreshed)
            }

            return refreshed
        }

    /**
     * Cleans up stale/orphan bundle files in the local bundles directory that are
     * not associated with any actively registered bundle.
     */
    fun pruneOrphanBundles() {
        val validFileNames = runCatching { installed.map { it.fileName }.toSet() }.getOrDefault(emptySet())
        localDir.listFiles()?.forEach { file ->
            if (file.isFile && file.name !in validFileNames && !file.name.endsWith(".tmp")) {
                file.setWritable(true)
                if (!file.delete()) {
                    runCatching {
                        Runtime.getRuntime().exec(arrayOf("su", "-c", "rm -f '${file.absolutePath}'")).waitFor()
                    }
                }
            }
        }
    }

    /**
     * Checks if io.github.nexfusion.patches is installed on the system as an APK with a strictly
     * newer versionCode or if it is missing from installed bundles and has not been explicitly uninstalled.
     */
    fun syncInstalledSystemPackage(): InstalledBundle? {
        val sysPkg = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo("io.github.nexfusion.patches", PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo("io.github.nexfusion.patches", 0)
            }
        }.getOrNull() ?: return null

        val appInfo = sysPkg.applicationInfo ?: return null
        val sourceDir = appInfo.publicSourceDir ?: appInfo.sourceDir ?: return null
        val sysApk = File(sourceDir)
        if (!sysApk.exists()) return null

        val sysVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) sysPkg.longVersionCode else @Suppress("DEPRECATION") sysPkg.versionCode.toLong()
        val currentList = installed
        val existing = currentList.find { it.id == "io.github.nexfusion.patches" }

        // If the bundle is not currently installed, verify if the user explicitly uninstalled it
        if (existing == null) {
            val uninstalledCode = localPrefs.getLong("uninstalled_${sysPkg.packageName}_code", -1L)
            val uninstalledTime = localPrefs.getLong("uninstalled_${sysPkg.packageName}_time", -1L)
            if (uninstalledCode != -1L && sysVersionCode <= uninstalledCode && sysApk.lastModified() <= uninstalledTime) {
                return null
            }
            return runCatching {
                sysApk.inputStream().use { stream ->
                    install(stream, "default")
                }
            }.getOrNull()
        }

        // Bundle is already installed: only upgrade from system package if the system APK is strictly NEWER,
        // or if the existing local file is missing/empty, or if reinstalled with newer modification time and different size.
        val existingFile = File(localDir, existing.fileName)
        val fileMissing = !existingFile.exists() || existingFile.length() == 0L
        val sysIsNewer = sysVersionCode > existing.versionCode
        val apkUpdated = sysVersionCode == existing.versionCode && existingFile.exists() &&
            (sysApk.lastModified() > existingFile.lastModified() && sysApk.length() != existingFile.length())

        if (fileMissing || sysIsNewer || apkUpdated) {
            return runCatching {
                sysApk.inputStream().use { stream ->
                    install(stream, existing.sourceId ?: "default")
                }
            }.getOrNull()
        }
        return null
    }

    fun updateInstalled(bundles: List<InstalledBundle>) {
        registryPrefs?.let { runCatching { BundleRegistry.write(it, bundles) } }
        BundleRegistry.write(localPrefs, bundles)
    }

    /**
     * Synchronizes local bundles and their APK files to the XposedService remote storage.
     * Ensures target hooked processes can access bundle APKs and registry metadata.
     */
    fun syncToService() {
        val svc = service ?: return
        val current = installed
        if (current.isNotEmpty()) {
            registryPrefs?.let { runCatching { BundleRegistry.write(it, current) } }
            for (bundle in current) {
                val localFile = File(localDir, bundle.fileName)
                if (localFile.exists()) {
                    runCatching {
                        svc.openRemoteFile(bundle.fileName).use { pfd ->
                            if (pfd.statSize == 0L || pfd.statSize != localFile.length()) {
                                FileOutputStream(pfd.fileDescriptor).use { output ->
                                    output.channel.truncate(0)
                                    localFile.inputStream().use { it.copyTo(output) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Installs or updates a bundle. Blocking; call off the main thread. */
    fun install(
        input: InputStream,
        sourceId: String?,
        totalBytes: Long = -1L,
        onProgress: ((downloaded: Long, total: Long) -> Unit)? = null,
    ): InstalledBundle {
        // Clean up any stale temporary bundle downloads older than 5 minutes
        context.cacheDir?.listFiles()?.forEach { file ->
            if (file.isFile && (file.name.endsWith(".tmp") || (file.name.startsWith("bundle") && file.name.endsWith(".apk")))) {
                if (System.currentTimeMillis() - file.lastModified() > 5 * 60 * 1000L) {
                    file.delete()
                }
            }
        }
        val tmp = File.createTempFile("bundle", ".apk", context.cacheDir)
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            tmp.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var downloaded = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    digest.update(buffer, 0, read)
                    output.write(buffer, 0, read)
                    downloaded += read
                    onProgress?.invoke(downloaded, totalBytes)
                }
            }
            val hash = digest.digest().joinToString("") { "%02x".format(it) }.take(16)

            val manifest = BundleManifest.read(context.packageManager, tmp)
            val compatibilityWarning = manifest.checkCompatibilityWarning()

            var clash = installed.find {
                it.id != manifest.id && manifest.resourcePackageId != 0 && it.resourcePackageId == manifest.resourcePackageId
            }
            var migratedClash: InstalledBundle? = null
            // A source that renamed its bundle, or seamless migration from a092devs to nexfusion:
            val isRenamedMigration = clash != null && (
                (clash.id == "io.github.nexfusion.patches.a092devs" && manifest.id == "io.github.nexfusion.patches") ||
                (sourceId != null && clash.sourceId == sourceId)
            )
            if (isRenamedMigration) {
                migratedClash = clash
                uninstall(clash!!)
                clash = null
            }
            if (clash != null) throw InvalidBundleException(
                "${manifest.name} uses the resource package id 0x%02x of ${clash.name}; both cannot be installed."
                    .format(manifest.resourcePackageId)
            )
            val fileName = "${manifest.id}-$hash.apk"
            val local = File(localDir, fileName)
            if (!local.exists() || local.length() != tmp.length()) {
                if (local.exists()) local.setWritable(true)
                tmp.copyTo(local, overwrite = true)
            }
            // Fails here, before anything is published, if the bundle's code cannot be loaded.
            val apps = loadApps(local, manifest.entryClass)

            service?.let { svc ->
                runCatching {
                    svc.openRemoteFile(fileName).use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { output ->
                            output.channel.truncate(0)
                            local.inputStream().use { it.copyTo(output) }
                        }
                    }
                }
            }

            val previous = installed.find { it.id == manifest.id } ?: migratedClash
            val bundle = InstalledBundle(
                id = manifest.id,
                name = manifest.name,
                version = manifest.version,
                versionCode = manifest.versionCode,
                author = manifest.author,
                description = manifest.description,
                fileName = fileName,
                enabled = previous?.enabled ?: true,
                sourceId = sourceId ?: previous?.sourceId,
                apps = apps.associate { it.packageName to it.appName },
                resourcePackageId = manifest.resourcePackageId,
                compatibilitySeries = manifest.compatibilitySeries,
                minManagerVersion = manifest.minManagerVersion,
                maxManagerVersion = manifest.maxManagerVersion,
                compatibilityWarning = compatibilityWarning,
                targets = apps.filter { it.targets.isNotEmpty() }.associate { it.packageName to it.targets },
                appIconColors = apps.mapNotNull { app -> app.appIconColor?.let { app.packageName to it } }.toMap(),
            )
            val updated = installed.filter { it.id != bundle.id } + bundle

            registryPrefs?.let { BundleRegistry.write(it, updated) }
            BundleRegistry.write(localPrefs, updated)

            // Clear uninstalled marker for this bundle ID
            localPrefs.edit()
                .remove("uninstalled_${manifest.id}_code")
                .remove("uninstalled_${manifest.id}_time")
                .apply()

            if (previous != null && previous.fileName != fileName) deleteFiles(previous.fileName)
            pruneOrphanBundles()
            return bundle
        } finally {
            tmp.delete()
        }
    }

    fun uninstall(bundle: InstalledBundle) {
        val updated = installed.filter { it.id != bundle.id }
        registryPrefs?.let { BundleRegistry.write(it, updated) }
        BundleRegistry.write(localPrefs, updated)
        deleteFiles(bundle.fileName)

        // Record that this bundle was explicitly uninstalled by the user
        // so syncInstalledSystemPackage does not immediately resurrect it.
        localPrefs.edit()
            .putLong("uninstalled_${bundle.id}_code", bundle.versionCode)
            .putLong("uninstalled_${bundle.id}_time", System.currentTimeMillis())
            .apply()

        pruneOrphanBundles()

        // If the uninstalled bundle is also installed on the system as an APK, remove it via root if available
        if (bundle.id == "io.github.nexfusion.patches") {
            runCatching {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "pm uninstall ${bundle.id}")).waitFor()
            }
        }
    }

    fun setEnabled(bundle: InstalledBundle, enabled: Boolean) {
        val updated = installed.map {
            if (it.id == bundle.id) it.copy(enabled = enabled) else it
        }
        registryPrefs?.let { BundleRegistry.write(it, updated) }
        BundleRegistry.write(localPrefs, updated)
    }

    val ignoredPackages: Set<String>
        get() {
            val remote = registryPrefs?.let { runCatching { it.getStringSet(PrefKeys.IGNORED_PACKAGES, null) }.getOrNull() }
            return remote ?: localPrefs.getStringSet(PrefKeys.IGNORED_PACKAGES, null) ?: io.github.nexfusion.updates.AppSettings(context).ignoredPackages
        }

    fun setPackageIgnored(packageName: String, ignored: Boolean) {
        val current = ignoredPackages.toMutableSet()
        if (ignored) current.add(packageName) else current.remove(packageName)
        localPrefs.edit().putStringSet(PrefKeys.IGNORED_PACKAGES, current).apply()
        registryPrefs?.let { runCatching { it.edit().putStringSet(PrefKeys.IGNORED_PACKAGES, current).apply() } }
        io.github.nexfusion.updates.AppSettings(context).setPackageIgnored(packageName, ignored)
    }

    /**
     * Installed apps patched by enabled bundles that are not yet in the module scope.
     *
     * Apps that are not installed or explicitly excluded/ignored by the user are left out.
     */
    fun missingScope(): List<String> {
        val svc = service ?: return emptyList()
        val scope = runCatching { svc.scope.toSet() }.getOrDefault(emptySet())
        val ignored = ignoredPackages
        return installed.filter { it.enabled }.flatMap { it.apps.keys }.distinct()
            .filter { it !in scope && isInstalled(it) && it !in ignored }
    }

    private fun isInstalled(packageName: String) =
        io.github.nexfusion.core.discovery.AppDiscovery.queryPackage(context, packageName).isInstalled

    fun requestScope(packages: List<String>, onResult: (String) -> Unit) {
        val svc = service ?: run {
            onResult("LSPosed framework service is currently inactive")
            return
        }
        svc.requestScope(packages, object : XposedService.OnScopeEventListener {
            override fun onScopeRequestApproved(approved: List<String>) {
                val message = when {
                    approved.isEmpty() -> "Scope updated"
                    approved.size == 1 -> {
                        val appLabel = io.github.nexfusion.core.discovery.AppDiscovery.queryPackage(context, approved[0]).appLabel
                        "Added $appLabel to module scope"
                    }
                    approved.size <= 2 -> {
                        val names = approved.map { io.github.nexfusion.core.discovery.AppDiscovery.queryPackage(context, it).appLabel }
                        "Added ${names.joinToString(", ")} to module scope"
                    }
                    else -> {
                        "Added ${approved.size} apps to module scope"
                    }
                }
                onResult(message)
            }

            override fun onScopeRequestFailed(message: String) = onResult(message)
        })
    }

    /** The patches of an installed bundle, loaded from the manager's copy of the bundle. */
    fun patches(bundle: InstalledBundle): List<AppPatchInfo> {
        val local = File(localDir, bundle.fileName)
        if (!local.exists()) {
            val svc = service ?: throw FileNotFoundException("Bundle ${bundle.name} file missing and service inactive")
            svc.openRemoteFile(bundle.fileName).use { pfd ->
                FileInputStream(pfd.fileDescriptor).use { input ->
                    local.outputStream().use { input.copyTo(it) }
                }
            }
        }
        val manifest = BundleManifest.read(context.packageManager, local)
        return loadApps(local, manifest.entryClass)
    }

    private fun loadApps(apk: File, entryClass: String): List<AppPatchInfo> =
        loadedApps.getOrPut(apk.name) {
            BundleClassLoader.load(apk, entryClass, BundleParentClassLoader(BundleManager::class.java.classLoader!!)).second
        }

    private fun deleteFiles(fileName: String) {
        service?.let { runCatching { it.deleteRemoteFile(fileName) } }
        val f = File(localDir, fileName)
        if (f.exists()) {
            f.setWritable(true)
            if (!f.delete()) {
                runCatching {
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "rm -f '${f.absolutePath}'")).waitFor()
                }
            }
        }
        loadedApps.remove(fileName)
    }

    private companion object {
        /** Bundles are loaded at most once per manager process; their file names change with every update. */
        val loadedApps = mutableMapOf<String, List<AppPatchInfo>>()
    }
}
