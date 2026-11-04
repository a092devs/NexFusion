package io.github.nexfusion.ui.screens

import kotlin.test.Test
import kotlin.test.assertEquals

class ChangelogMarkdownTest {

    @Test
    fun inlineCodeRendersWithoutBackticks() {
        assertEquals(
            listOf(
                MarkdownSpan("the "),
                MarkdownSpan("piko", code = true),
                MarkdownSpan(" bundle is removed"),
            ),
            parseMarkdownSpans("the `piko` bundle is removed"),
        )
    }

    @Test
    fun realisticReleaseNoteKeepsCodeRuns() {
        assertEquals(
            listOf(
                MarkdownSpan("X ("),
                MarkdownSpan("com.twitter.android", code = true),
                MarkdownSpan(" 12.35.0-prod.01): new app entry"),
            ),
            parseMarkdownSpans("X (`com.twitter.android` 12.35.0-prod.01): new app entry"),
        )
    }

    @Test
    fun boldBareUrlAndMarkdownLinkAreStyled() {
        assertEquals(
            listOf(
                MarkdownSpan("Full Changelog", bold = true),
                MarkdownSpan(": "),
                MarkdownSpan("https://example.com/compare", url = "https://example.com/compare"),
                MarkdownSpan(" see "),
                MarkdownSpan("the notes", url = "https://example.com/notes"),
            ),
            parseMarkdownSpans("**Full Changelog**: https://example.com/compare see [the notes](https://example.com/notes)"),
        )
    }

    @Test
    fun nestedCodeInsideBoldKeepsBothStyles() {
        assertEquals(
            listOf(
                MarkdownSpan("bold ", bold = true),
                MarkdownSpan("code", code = true, bold = true),
            ),
            parseMarkdownSpans("**bold `code`**"),
        )
    }

    @Test
    fun underscoresInsideIdentifiersStayPlain() {
        assertEquals(
            listOf(MarkdownSpan("intent_aware_ad_pivot and utm_x")),
            parseMarkdownSpans("intent_aware_ad_pivot and utm_x"),
        )
    }

    @Test
    fun bulletMarkerIsNotItalic() {
        assertEquals(listOf(MarkdownSpan("* item")), parseMarkdownSpans("* item"))
    }

    @Test
    fun italicAndStrikethrough() {
        assertEquals(
            listOf(
                MarkdownSpan("the "),
                MarkdownSpan("fast", italic = true),
                MarkdownSpan(" "),
                MarkdownSpan("gone", strikethrough = true),
                MarkdownSpan(" path"),
            ),
            parseMarkdownSpans("the *fast* ~~gone~~ path"),
        )
    }

    @Test
    fun trailingSentencePunctuationStaysOutOfUrl() {
        assertEquals(
            listOf(
                MarkdownSpan("see "),
                MarkdownSpan("https://example.com/x", url = "https://example.com/x"),
                MarkdownSpan("."),
            ),
            parseMarkdownSpans("see https://example.com/x."),
        )
    }

    @Test
    fun bulletAbsorbsSoftWrappedContinuationLines() {
        val notes = """
            - The `piko` bundle is removed: it carried only the X hooks
              for `12.19.1-release.0`, which no longer match.
            - Docs updated.
        """.trimIndent()
        assertEquals(
            listOf(
                ChangelogBlock(
                    "The `piko` bundle is removed: it carried only the X hooks for `12.19.1-release.0`, which no longer match.",
                    bullet = true,
                ),
                ChangelogBlock("Docs updated.", bullet = true),
            ),
            changelogBlocks(notes),
        )
    }

    @Test
    fun blankLinesKeepBlocksSeparate() {
        assertEquals(
            listOf(
                ChangelogBlock("first paragraph wraps"),
                ChangelogBlock(""),
                ChangelogBlock("second paragraph"),
            ),
            changelogBlocks("first paragraph\nwraps\n\nsecond paragraph"),
        )
    }

    @Test
    fun headersNeverMergeWithFollowingLines() {
        assertEquals(
            listOf(
                ChangelogBlock("1.0.0", header = true),
                ChangelogBlock(""),
                ChangelogBlock("item", bullet = true),
            ),
            changelogBlocks("## 1.0.0\n\n- item"),
        )
    }

    @Test
    fun htmlEntitiesAndAtomReleaseNotesCleanedToMarkdown() {
        val atomContent = """
            &lt;ul&gt;
            &lt;li&gt;&lt;code&gt;NexFusion&lt;/code&gt; 4.2.3:
            &lt;ul&gt;
            &lt;li&gt;Facebook Media Downloader Reliability:
            &lt;ul&gt;
            &lt;li&gt;Expanded post context menu anchors (&lt;code&gt;VideoFeedStoryMenuHelper&lt;/code&gt;).&lt;/li&gt;
            &lt;li&gt;Enhanced Reels more bottom sheet builders to inject top-level &quot;Download reel&quot;.&lt;/li&gt;
            &lt;/ul&gt;
            &lt;/li&gt;
            &lt;/ul&gt;
            &lt;/li&gt;
            &lt;/ul&gt;
            &lt;h3&gt;Compatibility&lt;/h3&gt;
            &lt;ul&gt;
            &lt;li&gt;&lt;strong&gt;NexFusion Series:&lt;/strong&gt; &lt;code&gt;3.6&lt;/code&gt; (minimum manager version: &lt;code&gt;&lt;/code&gt;)&lt;/li&gt;
            &lt;/ul&gt;
        """.trimIndent()

        val blocks = changelogBlocks(atomContent)
        val bulletTexts = blocks.filter { it.bullet }.map { it.text }
        assert(bulletTexts.any { it.contains("`NexFusion` 4.2.3:") })
        assert(bulletTexts.any { it.contains("Facebook Media Downloader Reliability:") })
        assert(bulletTexts.any { it.contains("Expanded post context menu anchors (`VideoFeedStoryMenuHelper`).") })
        assert(bulletTexts.any { it.contains("Enhanced Reels more bottom sheet builders to inject top-level \"Download reel\".") })
        assert(blocks.any { it.header && it.text == "Compatibility" })
        assert(bulletTexts.any { it.contains("**NexFusion Series:** `3.6`") && !it.contains("minimum manager version:") })
        for (b in blocks) {
            assert(!b.text.contains("&lt;") && !b.text.contains("&gt;") && !b.text.contains("<ul>") && !b.text.contains("<li>"))
        }
    }

    @Test
    fun nestedBulletsHaveHigherLevels() {
        val notes = """
            - Root item
              - Child item
                - Grandchild item
        """.trimIndent()
        val blocks = changelogBlocks(notes)
        assertEquals(3, blocks.size)
        assertEquals(0, blocks[0].level)
        assertEquals(1, blocks[1].level)
        assertEquals(2, blocks[2].level)
    }
}
