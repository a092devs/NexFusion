package io.github.nexfusion.manager

import io.github.nexfusion.updates.AppUpdateChecker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PatchSourceTest {
    private fun repo(url: String) = PatchSource.create("test", url).githubRepository

    @Test
    fun githubRepository() {
        assertEquals("2eno/NexFusion", repo("2eno/NexFusion"))
        assertEquals("2eno/NexFusion", repo("https://github.com/2eno/NexFusion"))
        assertEquals("2eno/NexFusion", repo("https://github.com/2eno/NexFusion/"))
        assertEquals("2eno/NexFusion", repo(" https://github.com/2eno/NexFusion.git "))
        assertEquals("some.org/patches_x", repo("some.org/patches_x"))
    }

    @Test
    fun jsonSourceIsNotARepository() {
        assertNull(repo("https://example.com/patches-bundle.json"))
        assertNull(repo("https://github.com/2eno/NexFusion/releases/latest"))
    }

    @Test
    fun versionCodeOfTag() {
        assertEquals(12, AppUpdateChecker.versionCodeOf("3.0.12"))
        assertEquals(6, AppUpdateChecker.versionCodeOf("3.0.6-alpha"))
        assertEquals(7, AppUpdateChecker.versionCodeOf("v3.0.7"))
        assertEquals(0, AppUpdateChecker.versionCodeOf("autobuild"))
    }

    @Test
    fun parseReleaseVersionExtractsSemverFromBundleAsset() {
        assertEquals(
            "4.2.2",
            SourceClient.parseReleaseVersion("beta", listOf("nexfusion-4.2.2.nxb" to "https://..."))
        )
        assertEquals(
            "4.2.2",
            SourceClient.parseReleaseVersion("4.2.2", listOf("nexfusion-4.2.2.nxb" to "https://..."))
        )
        assertEquals(
            "1.2.3",
            SourceClient.parseReleaseVersion("beta", listOf("bundle-1.2.3.nxb" to "https://..."))
        )
        assertEquals(
            "4.2.2-rc.1",
            SourceClient.parseReleaseVersion("beta", listOf("nexfusion-4.2.2-rc.1.nxb" to "https://..."))
        )
        assertEquals(
            "unknown",
            SourceClient.parseReleaseVersion("unknown", emptyList())
        )
    }
}
