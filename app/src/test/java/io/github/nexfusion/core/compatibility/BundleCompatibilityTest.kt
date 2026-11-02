package io.github.nexfusion.core.compatibility

import io.github.nexfusion.core.BundleManifest
import io.github.nexfusion.core.InstalledBundle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BundleCompatibilityTest {

    private fun createManifest(
        version: String = "3.2.0",
        series: String = "",
        minManager: String = "",
        maxManager: String = "",
    ) = BundleManifest(
        id = "io.github.nexfusion.patches",
        name = "NexFusion Patches",
        version = version,
        versionCode = 13L,
        entryClass = "io.github.nexfusion.nexfusion.NexFusionPatches",
        apiVersion = 1,
        author = "NexFusion",
        description = "Test bundle",
        resourcePackageId = 0x4e,
        compatibilitySeries = series,
        minManagerVersion = minManager,
        maxManagerVersion = maxManager,
    )

    @Test
    fun compatibleSeriesMatchesWithoutWarning() {
        val manifest = createManifest(series = "3.2", minManager = "3.2.0")
        assertNull(manifest.checkCompatibilityWarning("3.2.0"))
        assertNull(manifest.checkCompatibilityWarning("3.2.1"))
        assertNull(manifest.checkCompatibilityWarning("3.2.9"))
    }

    @Test
    fun incompatibleNewerSeriesReturnsAdvisoryWarning() {
        val manifest = createManifest(version = "3.3.0", series = "3.3", minManager = "3.3.0")
        val warning = manifest.checkCompatibilityWarning("3.2.0")
        assertNotNull(warning)
        assertTrue(warning.contains("NexFusion series 3.3"))
    }

    @Test
    fun olderSeriesPassesWhenApiCompatible() {
        val manifest = createManifest(version = "3.4.10", series = "3.4", minManager = "3.4.0")
        assertNull(manifest.checkCompatibilityWarning("3.5.0"))
    }

    @Test
    fun minManagerVersionReturnsAdvisoryWarning() {
        val manifest = createManifest(minManager = "3.2.5")
        val warning = manifest.checkCompatibilityWarning("3.2.0")
        assertNotNull(warning)
        assertTrue(warning.contains("3.2.5 or newer"))

        // Exactly at or above minManager passes without warning
        assertNull(manifest.checkCompatibilityWarning("3.2.5"))
        assertNull(manifest.checkCompatibilityWarning("3.2.6"))
    }

    @Test
    fun maxManagerVersionReturnsAdvisoryWarning() {
        val manifest = createManifest(maxManager = "3.2.5")
        assertNull(manifest.checkCompatibilityWarning("3.2.0"))
        assertNull(manifest.checkCompatibilityWarning("3.2.5"))
        val warning = manifest.checkCompatibilityWarning("3.2.6")
        assertNotNull(warning)
        assertTrue(warning.contains("3.2.5 or older"))
    }

    @Test
    fun emptyCompatibilityMetadataAlwaysPasses() {
        val legacyManifest = createManifest(series = "", minManager = "", maxManager = "")
        assertNull(legacyManifest.checkCompatibilityWarning("3.2.0"))
        assertNull(legacyManifest.checkCompatibilityWarning("1.0.0"))
        assertNull(legacyManifest.checkCompatibilityWarning("4.0.0"))
    }

    @Test
    fun installedBundleCompatibilitySerialization() {
        val bundle = InstalledBundle(
            id = "io.github.nexfusion.patches",
            name = "NexFusion Patches",
            version = "3.2.0",
            versionCode = 13L,
            author = "NexFusion",
            description = "Test",
            fileName = "nexfusion-1234.apk",
            enabled = true,
            sourceId = "default",
            apps = mapOf("com.instagram.android" to "Instagram"),
            resourcePackageId = 0x4e,
            compatibilitySeries = "3.2",
            minManagerVersion = "3.2.0",
            maxManagerVersion = "",
            compatibilityWarning = "Update recommended",
        )

        val json = bundle.toJson()
        val restored = InstalledBundle.fromJson(json)

        assertEquals("3.2", restored.compatibilitySeries)
        assertEquals("3.2.0", restored.minManagerVersion)
        assertEquals("", restored.maxManagerVersion)
        assertEquals("Update recommended", restored.compatibilityWarning)
    }
}

