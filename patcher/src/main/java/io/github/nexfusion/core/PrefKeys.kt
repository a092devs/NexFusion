package io.github.nexfusion.core

/** Centralized keys for SharedPreferences and RemotePreferences used across NexFusion. */
object PrefKeys {
    /** Remote preferences group shared with hooked apps for bundle registry. */
    const val BUNDLES_GROUP = "bundles"

    /** Key for the installed bundles JSON array in bundle registry. */
    const val BUNDLES_INSTALLED = "installed"

    /** Remote preferences key for ignored/excluded packages. */
    const val IGNORED_PACKAGES = "ignored_packages"

    /** Per-app remote preferences key for a patch's enabled state.
     *  Format: bundleId/patchName */
    fun patchEnabled(bundleId: String, patchName: String) = "$bundleId/$patchName"

    /** Cache key for the last failing-patch set; see [PatchSession.finish]. */
    const val FAILED_PATCHES = "nexfusion:failed"

    /** Cache key for bundle fingerprint validity token.
     *  Format: nexfusion:id:{bundleId} = {appLastUpdateTime}-{coreVersion}-{bundleVersion}-{bundleHash} */
    fun bundleCacheId(bundleId: String) = "nexfusion:id:$bundleId"

    /** SharedPreferences file name for DexKit cache. */
    const val DEXKIT_CACHE = "nexfusion_dexkit"

    /** SharedPreferences file name for general app preferences. */
    const val APP_PREFS = "prefs"

    /** SharedPreferences key for notification permission asked flag. */
    const val NOTIFICATION_PERMISSION_ASKED = "notification_permission_asked"

    /** Remote preferences group for patch sources. */
    const val SOURCES_GROUP = "sources"

    /** Remote preferences key for source release cache. */
    const val SOURCES_RELEASE_CACHE = "sources_release_cache"
}