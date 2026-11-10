package io.github.nexfusion.core

import android.content.Context
import android.content.pm.PackageInfo
import dalvik.system.DexClassLoader
import android.content.pm.PackageManager
import android.os.Build
import io.github.nexfusion.AppPatchInfo
import io.github.nexfusion.PATCHER_API_VERSION
import io.github.nexfusion.PatchBundle
import java.io.File

import io.github.nexfusion.core.compatibility.VersionComparator

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
            @Suppress("DEPRECATION")
            val info: PackageInfo = packageManager.getPackageArchiveInfo(
                apk.path, PackageManager.GET_META_DATA
            ) ?: throw InvalidBundleException("Not an APK file")
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
        "io.github.nexfusion.",
        "org.luckypray.dexkit.",
        "com.google.flatbuffers.",
        "androidx.annotation.",
        listOf("de", "robv", "android", "xposed").joinToString(".") + ".",
        "io.github.libxposed.",
    ) + extraSharedPrefixes

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        if (sharedPrefixes.any { name.startsWith(it) }) return module.loadClass(name)
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
        val loader = DexClassLoader(path, null, null, parent)
        val entry = try {
            val clazz = loader.loadClass(entryClass)
            // A Kotlin object exposes its instance as INSTANCE.
            runCatching { clazz.getField("INSTANCE").get(null) }.getOrNull()
                ?: clazz.getDeclaredConstructor().newInstance()
        } catch (e: Throwable) {
            throw InvalidBundleException("Cannot instantiate bundle entry $entryClass", e)
        }
        if (entry !is PatchBundle) throw InvalidBundleException("$entryClass does not implement PatchBundle")
        return loader to entry.apps
    }
}
