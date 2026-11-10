package io.github.nexfusion.updates

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppUpdateCheckerTest {

    @Test
    fun parseAtomFeedExtractsLatestReleaseAndApk() {
        val sampleXml = """
<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <entry>
    <link rel="alternate" type="text/html" href="https://github.com/a092devs/NexFusion/releases/tag/4.0.0"/>
    <title>4.0.0</title>
    <content type="html">&lt;p&gt;Responsive UI Layout&lt;/p&gt;</content>
  </entry>
  <entry>
    <link rel="alternate" type="text/html" href="https://github.com/a092devs/NexFusion/releases/tag/3.6.3"/>
    <title>3.6.3</title>
    <content type="html">&lt;p&gt;UI Rollback&lt;/p&gt;</content>
  </entry>
</feed>
""".trimIndent()

        val release = AppUpdateChecker.parseAtomFeed(sampleXml, "a092devs/NexFusion")
        assertNotNull(release)
        assertEquals("4.0.0", release.tag)
        assertEquals(0, release.versionCode)
        assertTrue(release.apkUrl?.endsWith(".apk") == true)
        assertEquals("https://github.com/a092devs/NexFusion/releases/tag/4.0.0", release.pageUrl)
    }

    @Test
    fun parseApiResponsePrefersReleaseApkOverDebugApk() {
        val sampleJson = """
[
  {
    "tag_name": "4.0.0",
    "draft": false,
    "prerelease": false,
    "body": "Major release",
    "html_url": "https://github.com/a092devs/NexFusion/releases/tag/4.0.0",
    "assets": [
      {
        "name": "NexFusion-4.0.0-debug.apk",
        "browser_download_url": "https://github.com/a092devs/NexFusion/releases/download/4.0.0/NexFusion-4.0.0-debug.apk"
      },
      {
        "name": "NexFusion-4.0.0.apk",
        "browser_download_url": "https://github.com/a092devs/NexFusion/releases/download/4.0.0/NexFusion-4.0.0.apk"
      }
    ]
  }
]
""".trimIndent()

        val release = AppUpdateChecker.parseApiResponse(sampleJson)
        assertNotNull(release)
        assertEquals("4.0.0", release.tag)
        assertEquals(0, release.versionCode)
        assertEquals("https://github.com/a092devs/NexFusion/releases/download/4.0.0/NexFusion-4.0.0.apk", release.apkUrl)
    }

    @Test
    fun parseApiResponsePrefersDebugApkWhenRequested() {
        val sampleJson = """
[
  {
    "tag_name": "4.0.0",
    "draft": false,
    "prerelease": false,
    "body": "Major release",
    "html_url": "https://github.com/a092devs/NexFusion/releases/tag/4.0.0",
    "assets": [
      {
        "name": "NexFusion-4.0.0.apk",
        "browser_download_url": "https://github.com/a092devs/NexFusion/releases/download/4.0.0/NexFusion-4.0.0.apk"
      },
      {
        "name": "NexFusion-4.0.0-debug.apk",
        "browser_download_url": "https://github.com/a092devs/NexFusion/releases/download/4.0.0/NexFusion-4.0.0-debug.apk"
      }
    ]
  }
]
""".trimIndent()

        val release = AppUpdateChecker.parseApiResponse(sampleJson, buildType = AppSettings.BUILD_TYPE_DEBUG)
        assertNotNull(release)
        assertEquals("4.0.0", release.tag)
        assertEquals(0, release.versionCode)
        assertEquals("https://github.com/a092devs/NexFusion/releases/download/4.0.0/NexFusion-4.0.0-debug.apk", release.apkUrl)
    }

    @Test
    fun parseApiResponseBetaReleaseIncludesCommitishAndPublishedDate() {
        val sampleJson = """
[
  {
    "tag_name": "beta",
    "target_commitish": "49c923b08ab15b4970ec74daef8cbe1fdb57aeae",
    "draft": false,
    "prerelease": true,
    "published_at": "2026-11-03T14:40:00Z",
    "body": "v4.2.2 beta",
    "html_url": "https://github.com/a092devs/NexFusion/releases/tag/beta",
    "assets": [
      {
        "name": "NexFusion-4.2.2.apk",
        "browser_download_url": "https://github.com/a092devs/NexFusion/releases/download/beta/NexFusion-4.2.2.apk"
      }
    ]
  }
]
""".trimIndent()

        val release = AppUpdateChecker.parseApiResponse(sampleJson)
        assertNotNull(release)
        assertEquals("beta", release.tag)
        assertEquals("49c923b08ab15b4970ec74daef8cbe1fdb57aeae", release.targetCommitish)
        assertTrue(release.publishedAt > 0L)
    }
}
