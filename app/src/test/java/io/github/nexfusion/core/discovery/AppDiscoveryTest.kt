package io.github.nexfusion.core.discovery

import kotlin.test.Test
import kotlin.test.assertEquals

class AppDiscoveryTest {

    @Test
    fun bundleCardsListOnlyAppsInstalledOnThisDevice() {
        val apps = mapOf(
            "com.instagram.android" to "Instagram",
            "tv.twitch.android.app" to "Twitch",
            "com.google.android.youtube" to "YouTube",
        )
        assertEquals(
            listOf("Instagram", "YouTube"),
            AppDiscovery.installedAppNames(apps) { it != "tv.twitch.android.app" },
        )
    }

    @Test
    fun namesAreSortedAndDeduplicated() {
        val apps = mapOf(
            "zeta.pkg" to "Zeta",
            "alpha.pkg" to "alpha",
            "alpha.other" to "alpha",
        )
        assertEquals(listOf("alpha", "Zeta"), AppDiscovery.installedAppNames(apps) { true })
    }

    @Test
    fun aBundleWithNoInstalledAppsHasNoNames() {
        assertEquals(
            emptyList(),
            AppDiscovery.installedAppNames(mapOf("tv.twitch.android.app" to "Twitch")) { false },
        )
    }
}
