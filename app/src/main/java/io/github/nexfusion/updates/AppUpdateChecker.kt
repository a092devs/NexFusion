package io.github.nexfusion.updates

import io.github.nexfusion.core.BuildConfig
import io.github.nexfusion.core.compatibility.VersionComparator
import io.github.nexfusion.manager.SourceClient
import org.json.JSONArray

/** A release of NexFusion itself. */
class AppRelease(
    val tag: String,
    val versionCode: Int,
    val notes: String,
    val pageUrl: String,
    /** Download URL of the APK asset, if the release has one. */
    val apkUrl: String?,
    val isPrerelease: Boolean = false,
) {
    val isNewer get() = if (tag.equals("beta", ignoreCase = true)) {
        true
    } else {
        VersionComparator.compare(tag, BuildConfig.VERSION_NAME) > 0 || (versionCode > BuildConfig.VERSION_CODE)
    }
}

/** Checks the GitHub releases of NexFusion for updates. Blocking; call off the main thread. */
object AppUpdateChecker {
    /** The version code a release tag `X.Y.Z` or `X.Y.Z-suffix` stands for, Z. 0 for other tags, e.g. nightly builds. */
    fun versionCodeOf(tag: String): Int =
        Regex("""v?\d+\.\d+\.(\d+)(-.*)?""").matchEntire(tag)?.groupValues?.get(1)?.toInt() ?: 0

    /** The latest release, respecting the selected channel (stable vs beta pre-releases). */
    fun latest(includePrerelease: Boolean = false): AppRelease? {
        val repo = BuildConfig.UPDATE_REPOSITORY
        // Try the GitHub REST API first
        val apiResult = runCatching {
            val response = SourceClient.get("https://api.github.com/repos/$repo/releases?per_page=20")
            parseApiResponse(response, includePrerelease)
        }
        if (apiResult.isSuccess && apiResult.getOrNull() != null) {
            return apiResult.getOrNull()
        }

        // Fallback: parse releases.atom directly from github.com (immune to GitHub API rate limits)
        return runCatching {
            val atomXml = SourceClient.get("https://github.com/$repo/releases.atom")
            parseAtomFeed(atomXml, repo, includePrerelease)
        }.getOrElse { fallbackError ->
            throw apiResult.exceptionOrNull() ?: fallbackError
        }
    }

    internal fun parseApiResponse(jsonText: String, includePrerelease: Boolean = false): AppRelease? {
        val releases = JSONArray(jsonText)
        val candidates = (0 until releases.length()).map { releases.getJSONObject(it) }
            .filter { !it.optBoolean("draft") }
            .filter {
                val tag = it.optString("tag_name", "")
                tag.isNotBlank() && (tag.equals("beta", ignoreCase = true) || VersionComparator.compare(tag, "0.0.0") > 0)
            }
        val allowed = if (includePrerelease) {
            candidates
        } else {
            val stable = candidates.filter { !it.optBoolean("prerelease") && !it.optString("tag_name", "").equals("beta", ignoreCase = true) }
            stable.ifEmpty { candidates }
        }
        val release = allowed.maxWithOrNull { a, b ->
            val tagA = a.optString("tag_name", "")
            val tagB = b.optString("tag_name", "")
            if (tagA.equals("beta", ignoreCase = true) && !tagB.equals("beta", ignoreCase = true)) 1
            else if (tagB.equals("beta", ignoreCase = true) && !tagA.equals("beta", ignoreCase = true)) -1
            else VersionComparator.compare(tagA, tagB)
        } ?: return null
        val assets = release.getJSONArray("assets")
        val allApks = (0 until assets.length()).map { assets.getJSONObject(it) }
            .filter { it.getString("name").endsWith(".apk") }
        val apk = allApks.firstOrNull { !it.getString("name").contains("-debug.apk", ignoreCase = true) }
            ?: allApks.firstOrNull()
        return AppRelease(
            tag = release.getString("tag_name"),
            versionCode = versionCodeOf(release.getString("tag_name")),
            notes = release.optString("body"),
            pageUrl = release.getString("html_url"),
            apkUrl = apk?.getString("browser_download_url"),
            isPrerelease = release.optBoolean("prerelease") || release.optString("tag_name", "").equals("beta", ignoreCase = true),
        )
    }

    internal fun parseAtomFeed(atomXml: String, repo: String, includePrerelease: Boolean = false): AppRelease? {
        val entryRegex = Regex("""<entry>([\s\S]*?)</entry>""")
        val titleRegex = Regex("""<title>([^<]+)</title>""")
        val linkRegex = Regex("""<link[^>]*href="([^"]+)"""")
        val contentRegex = Regex("""<content[^>]*>([\s\S]*?)</content>""")

        data class AtomEntry(val tag: String, val pageUrl: String, val content: String, val isPre: Boolean)

        val entries = entryRegex.findAll(atomXml).mapNotNull { match ->
            val block = match.groupValues[1]
            val link = linkRegex.find(block)?.groupValues?.get(1).orEmpty()
            val tag = link.substringAfterLast("/releases/tag/", "").takeIf { it.isNotEmpty() }
                ?: titleRegex.find(block)?.groupValues?.get(1)?.trim().orEmpty()
            if (tag.isEmpty() || (!tag.equals("beta", ignoreCase = true) && VersionComparator.compare(tag, "0.0.0") <= 0)) return@mapNotNull null
            val content = contentRegex.find(block)?.groupValues?.get(1).orEmpty()
            val isPre = tag.contains('-') || tag.equals("autobuild", ignoreCase = true) || tag.equals("beta", ignoreCase = true)
            AtomEntry(tag, link, content, isPre)
        }.toList()

        if (entries.isEmpty()) return null

        val chosen = if (includePrerelease) {
            entries.maxWithOrNull { a, b ->
                if (a.tag.equals("beta", ignoreCase = true) && !b.tag.equals("beta", ignoreCase = true)) 1
                else if (b.tag.equals("beta", ignoreCase = true) && !a.tag.equals("beta", ignoreCase = true)) -1
                else VersionComparator.compare(a.tag, b.tag)
            }
        } else {
            val stable = entries.filter { !it.isPre }
            (stable.ifEmpty { entries }).maxWithOrNull { a, b ->
                VersionComparator.compare(a.tag, b.tag)
            }
        } ?: return null

        var apkUrl: String? = null
        val expandedAssets = runCatching {
            SourceClient.get("https://github.com/$repo/releases/expanded_assets/${chosen.tag}")
        }.getOrNull()

        if (expandedAssets != null) {
            val apkMatches = Regex("""href="(/[^"]+/releases/download/[^"]+?\.apk)"""").findAll(expandedAssets)
                .map { "https://github.com" + it.groupValues[1] }
                .toList()
            apkUrl = apkMatches.firstOrNull { !it.contains("-debug.apk", ignoreCase = true) }
                ?: apkMatches.firstOrNull()
        }

        if (apkUrl == null) {
            apkUrl = "https://github.com/$repo/releases/download/${chosen.tag}/NexFusion-${chosen.tag}.apk"
        }

        return AppRelease(
            tag = chosen.tag,
            versionCode = versionCodeOf(chosen.tag),
            notes = chosen.content,
            pageUrl = chosen.pageUrl,
            apkUrl = apkUrl,
        )
    }
}
