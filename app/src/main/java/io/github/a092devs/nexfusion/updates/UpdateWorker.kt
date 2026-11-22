package io.github.a092devs.nexfusion.updates

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.a092devs.nexfusion.core.CacheManager
import io.github.a092devs.nexfusion.manager.BundleManager
import io.github.a092devs.nexfusion.manager.SourceClient
import io.github.a092devs.nexfusion.manager.SourceStore
import java.util.concurrent.TimeUnit

/** Checks for updates of NexFusion and of the installed patch sources in the background, and notifies about or applies them. */
class UpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val settings = AppSettings(context)
        val includeBeta = settings.updateChannel == AppSettings.UPDATE_CHANNEL_BETA

        if (settings.notifyAppUpdates) {
            runCatching { AppUpdateChecker.latest(includeBeta, settings.updateBuildType) }.getOrNull()
                ?.takeIf { it.isNewer && settings.lastNotified(KEY_APP) != it.tag }
                ?.let { release ->
                    UpdateNotifications.appUpdate(context, release)
                    settings.setLastNotified(KEY_APP, release.tag)
                }
        }

        val store = SourceStore(context)
        val bundleManager = BundleManager(context)
        val installedSources = store.all().filter { it.enabled && it.installedVersion != null }

        val autoUpdated = mutableListOf<String>()
        val notified = mutableListOf<String>()

        for (source in installedSources) {
            val release = runCatching { SourceClient.latestRelease(source, includePrerelease = includeBeta) }.getOrNull() ?: continue
            val key = "source_${source.id}"
            if (release.version == source.installedVersion) continue

            if (settings.autoUpdatePatches) {
                val success = runCatching {
                    for ((_, url) in release.bundles) {
                        val connection = SourceClient.open(url)
                        connection.inputStream.use { stream ->
                            bundleManager.install(stream, source.id, connection.contentLengthLong)
                        }
                    }
                    store.save(source.copy(installedVersion = release.version))
                    settings.setLastNotified(key, release.version)
                    true
                }.getOrDefault(false)

                if (success) {
                    autoUpdated.add("${source.name} ${release.version}")
                }
            } else if (settings.notifyPatchUpdates) {
                if (settings.lastNotified(key) != release.version) {
                    settings.setLastNotified(key, release.version)
                    notified.add("${source.name} ${release.version}")
                }
            }
        }

        if (autoUpdated.isNotEmpty()) {
            runCatching {
                io.github.a092devs.nexfusion.activity.SettingApplication.mService?.let {
                    bundleManager.syncToService()
                }
            }
            UpdateNotifications.patchesAutoUpdated(context, autoUpdated)
        } else if (notified.isNotEmpty()) {
            UpdateNotifications.patchUpdates(context, notified)
        }

        // Lightweight periodic cache prune to prevent storage accumulation
        runCatching { CacheManager.prune(context) }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "update_check"
        private const val KEY_APP = "app"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<UpdateWorker>(6, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun checkNow(context: Context) {
            val request = androidx.work.OneTimeWorkRequestBuilder<UpdateWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork("update_check_now", androidx.work.ExistingWorkPolicy.REPLACE, request)
        }
    }
}
