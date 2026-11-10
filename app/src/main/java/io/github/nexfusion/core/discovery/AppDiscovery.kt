package io.github.nexfusion.core.discovery

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.github.nexfusion.AppTarget
import io.github.nexfusion.core.InstalledBundle
import io.github.nexfusion.core.compatibility.CompatibilityEngine
import io.github.nexfusion.core.compatibility.CompatibilityStatus
import io.github.nexfusion.manager.PatchSource

/**
 * Package metadata queried live from Android's PackageManager.
 */
data class PackageLiveInfo(
    val packageName: String,
    val appLabel: String,
    val versionName: String?,
    val versionCode: Long,
    val isInstalled: Boolean,
)

/**
 * Consolidated UI and business model representing an application in NexFusion.
 */
data class AppItemState(
    val packageName: String,
    val appName: String,
    val isInstalled: Boolean,
    val installedVersionName: String?,
    val installedVersionCode: Long?,
    val recommendedVersion: String?,
    val appIconColor: Int?,
    val compatibility: CompatibilityStatus,
    val inScope: Boolean,
    val bundles: List<InstalledBundle>,
    /** Names of configured sources providing bundles for this application. */
    val sourceNames: List<String> = emptyList(),
    /** Indicates whether multiple bundles provide patches for this application, causing potential conflicts. */
    val hasConflicts: Boolean = false,
    /** Whether this application is explicitly excluded/ignored from patching by the user. */
    val isExcluded: Boolean = false,
)

object AppDiscovery {

    /**
     * Inspects the device PackageManager to retrieve live package details.
     */
    fun queryPackage(context: Context, packageName: String): PackageLiveInfo {
        val pm = context.packageManager

        val info = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
        }.onFailure {
            android.util.Log.w("NexFusionDiscovery", "pm.getPackageInfo(0) failed for $packageName: ${it.javaClass.simpleName} - ${it.message}")
        }.recoverCatching {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                PackageManager.MATCH_UNINSTALLED_PACKAGES or 0x00020000
            } else 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, flags)
            }
        }.onFailure {
            android.util.Log.w("NexFusionDiscovery", "pm.getPackageInfo(MATCH_ALL) failed for $packageName: ${it.javaClass.simpleName} - ${it.message}")
        }.recoverCatching {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getPackageArchiveInfo(appInfo.sourceDir, 0)?.apply {
                this.applicationInfo = appInfo
            } ?: throw it
        }.onFailure {
            android.util.Log.w("NexFusionDiscovery", "pm.getApplicationInfo failed for $packageName: ${it.javaClass.simpleName} - ${it.message}")
        }.getOrNull()

        if (info != null) {
            val appInfo = info.applicationInfo
            val enabledSetting = runCatching { pm.getApplicationEnabledSetting(packageName) }
                .getOrDefault(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
            val isEnabled = (appInfo?.enabled ?: true) &&
                enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED &&
                enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER &&
                enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED

            val label = appInfo?.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: packageName

            val isInstalledForUser = if (appInfo != null) {
                (appInfo.flags and ApplicationInfo.FLAG_INSTALLED) != 0
            } else true

            if (!isEnabled || !isInstalledForUser) {
                android.util.Log.i("NexFusionDiscovery", "$packageName is disabled or uninstalled for user - treating as uninstalled")
                return PackageLiveInfo(
                    packageName = packageName,
                    appLabel = label,
                    versionName = info.versionName,
                    versionCode = 0L,
                    isInstalled = false,
                )
            }

            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION") info.versionCode.toLong()
            }
            android.util.Log.i("NexFusionDiscovery", "Successfully resolved $packageName: version=${info.versionName}")
            return PackageLiveInfo(
                packageName = packageName,
                appLabel = label,
                versionName = info.versionName,
                versionCode = versionCode,
                isInstalled = true,
            )
        }

        val hasIntent = runCatching { pm.getLaunchIntentForPackage(packageName) }.getOrNull() != null ||
            runCatching {
                val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).setPackage(packageName)
                pm.queryIntentActivities(intent, 0).isNotEmpty()
            }.getOrDefault(false)

        if (hasIntent) {
            val appInfo = runCatching { pm.getApplicationInfo(packageName, 0) }.getOrNull()
            val enabledSetting = runCatching { pm.getApplicationEnabledSetting(packageName) }
                .getOrDefault(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
            val isEnabled = (appInfo?.enabled ?: true) &&
                enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED &&
                enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER &&
                enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED

            val label = appInfo?.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: packageName

            val isInstalledForUser = if (appInfo != null) {
                (appInfo.flags and ApplicationInfo.FLAG_INSTALLED) != 0
            } else true

            if (!isEnabled || !isInstalledForUser) {
                return PackageLiveInfo(
                    packageName = packageName,
                    appLabel = label,
                    versionName = null,
                    versionCode = 0L,
                    isInstalled = false,
                )
            }

            android.util.Log.i("NexFusionDiscovery", "Intent fallback resolved $packageName: label=$label")
            return PackageLiveInfo(
                packageName = packageName,
                appLabel = label,
                versionName = "installed",
                versionCode = 0L,
                isInstalled = true,
            )
        }

        android.util.Log.w("NexFusionDiscovery", "Failed to resolve $packageName - marked Not Installed")
        return PackageLiveInfo(
            packageName = packageName,
            appLabel = packageName,
            versionName = null,
            versionCode = 0L,
            isInstalled = false,
        )
    }

    /**
     * Loads the application icon as an ImageBitmap, or null if uninstalled/unavailable.
     */
    fun loadAppIcon(context: Context, packageName: String): ImageBitmap? = runCatching {
        val pm = context.packageManager
        val drawable = pm.getApplicationIcon(packageName)
        val bitmap = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, 192, 192)
        drawable.draw(canvas)
        bitmap.asImageBitmap()
    }.getOrNull()

    data class KnownSupportedApp(
        val packageName: String,
        val appName: String,
        val defaultBundleName: String,
        val appIconColor: Int?,
        val targets: List<AppTarget> = emptyList(),
    )

    val KNOWN_SUPPORTED_APPS = listOf(
        KnownSupportedApp(
            packageName = "com.google.android.youtube",
            appName = "YouTube",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0xFF0033,
        ),
        KnownSupportedApp(
            packageName = "com.google.android.apps.youtube.music",
            appName = "YouTube Music",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0xFF0000,
        ),
        KnownSupportedApp(
            packageName = "com.reddit.frontpage",
            appName = "Reddit",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0xFF4500,
        ),
        KnownSupportedApp(
            packageName = "com.google.android.apps.photos",
            appName = "Google Photos",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x4285F4,
        ),
        KnownSupportedApp(
            packageName = "ch.protonvpn.android",
            appName = "Proton VPN",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x6D4AFF,
        ),
        KnownSupportedApp(
            packageName = "com.google.android.inputmethod.latin",
            appName = "Gboard",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x4285F4,
        ),
        KnownSupportedApp(
            packageName = "com.google.android.gm",
            appName = "Gmail",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0xEA4335,
        ),
        KnownSupportedApp(
            packageName = "com.google.android.googlequicksearchbox",
            appName = "Google (Discover)",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x4285F4,
        ),
        KnownSupportedApp(
            packageName = "com.google.android.apps.maps",
            appName = "Google Maps",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x34A853,
        ),
        KnownSupportedApp(
            packageName = "com.facebook.katana",
            appName = "Facebook",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x0866FF,
        ),
        KnownSupportedApp(
            packageName = "com.crunchyroll.crunchyroid",
            appName = "Crunchyroll",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0xFF5C00,
        ),
        KnownSupportedApp(
            packageName = "com.discord",
            appName = "Discord",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x5865F2,
        ),
        KnownSupportedApp(
            packageName = "com.linkedin.android",
            appName = "LinkedIn",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x0A66C2,
        ),
        KnownSupportedApp(
            packageName = "com.instagram.android",
            appName = "Instagram",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0xE1306C,
        ),
        KnownSupportedApp(
            packageName = "com.twitter.android",
            appName = "X",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x000000,
        ),
        KnownSupportedApp(
            packageName = "org.telegram.messenger",
            appName = "Telegram",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x2AABEE,
        ),
        KnownSupportedApp(
            packageName = "org.telegram.messenger.web",
            appName = "Telegram (Web)",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x2AABEE,
        ),
        KnownSupportedApp(
            packageName = "com.kick.mobile",
            appName = "Kick",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x53FC18,
        ),
        KnownSupportedApp(
            packageName = "tv.twitch.android.app",
            appName = "Twitch",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x9146FF,
        ),
        KnownSupportedApp(
            packageName = "org.videolan.vlc",
            appName = "VLC",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0xFF6600,
        ),
        KnownSupportedApp(
            packageName = "com.stremio.one",
            appName = "Stremio",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x5B67F1,
        ),
        KnownSupportedApp(
            packageName = "com.spotify.music",
            appName = "Spotify",
            defaultBundleName = "NexFusion Patches",
            appIconColor = 0x1ED760,
        ),
    )

    /**
     * The display names of [apps] (package name to display name) whose packages are present on
     * this device, sorted for display. Cards describing a bundle use this, so a bundle stops
     * naming apps that are not installed; [isInstalled] is a parameter so the rule is testable.
     */
    fun installedAppNames(apps: Map<String, String>, isInstalled: (String) -> Boolean): List<String> =
        apps.filterKeys { isInstalled(it) }.values.distinct().sortedBy { it.lowercase() }

    /**
     * Discovers all target applications from enabled bundles across enabled sources,
     * combining them with live package information and the supported app catalog.
     */
    fun buildCatalog(
        context: Context,
        bundles: List<InstalledBundle>,
        missingScope: Set<String>,
        sources: List<PatchSource> = emptyList(),
        ignoredPackages: Set<String> = emptySet(),
    ): List<AppItemState> {
        val enabledSourceIds = sources.filter { it.enabled }.map { it.id }.toSet()
        // A bundle is active only if enabled AND its source (if assigned) is enabled
        val activeBundles = bundles.filter { bundle ->
            bundle.enabled && (bundle.sourceId == null || sources.isEmpty() || bundle.sourceId in enabledSourceIds)
        }

        // Aggregate all target package names declared by active bundles
        val packageMap = mutableMapOf<String, String>()
        for (bundle in activeBundles) {
            for ((pkg, name) in bundle.apps) {
                if (!packageMap.containsKey(pkg) || packageMap[pkg] == pkg) {
                    packageMap[pkg] = name
                }
            }
        }

        // Also ensure all known supported applications are present in the catalog
        for (known in KNOWN_SUPPORTED_APPS) {
            if (!packageMap.containsKey(known.packageName)) {
                packageMap[known.packageName] = known.appName
            }
        }

        return packageMap.map { (packageName, declaredName) ->
            val live = queryPackage(context, packageName)
            val matchingBundles = activeBundles.filter { packageName in it.apps }
            val known = KNOWN_SUPPORTED_APPS.find { it.packageName == packageName }

            val allTargets: List<AppTarget> = if (matchingBundles.isNotEmpty()) {
                matchingBundles.flatMap { it.targets[packageName].orEmpty() }
            } else {
                known?.targets.orEmpty()
            }

            val compatibility = if (matchingBundles.isNotEmpty()) {
                CompatibilityEngine.resolve(live.versionName, allTargets)
            } else if (!live.isInstalled) {
                CompatibilityStatus.NotInstalled(CompatibilityEngine.recommendedVersion(allTargets))
            } else {
                CompatibilityStatus.MissingBundle
            }

            val recommended = CompatibilityEngine.recommendedVersion(allTargets)
            val brandColor = matchingBundles.firstNotNullOfOrNull { it.appIconColors[packageName] } ?: known?.appIconColor

            val sourceNames = matchingBundles.mapNotNull { b ->
                sources.find { it.id == b.sourceId }?.name ?: b.sourceId
            }.distinct().ifEmpty {
                listOfNotNull(known?.defaultBundleName)
            }

            val hasConflicts = matchingBundles.size > 1

            AppItemState(
                packageName = packageName,
                appName = if (live.isInstalled && live.appLabel.isNotBlank() && live.appLabel != packageName) {
                    live.appLabel
                } else {
                    declaredName
                },
                isInstalled = live.isInstalled,
                installedVersionName = live.versionName,
                installedVersionCode = live.versionCode,
                recommendedVersion = recommended,
                appIconColor = brandColor,
                compatibility = compatibility,
                inScope = !missingScope.contains(packageName),
                bundles = matchingBundles,
                sourceNames = sourceNames,
                hasConflicts = hasConflicts,
                isExcluded = packageName in ignoredPackages,
            )
        }.sortedWith(compareBy({ !it.isInstalled }, { it.appName.lowercase() }))
    }
}
