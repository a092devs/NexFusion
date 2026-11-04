package io.github.nexfusion.ui.screens

/**
 * One run of changelog text with the inline markdown styling that applies to it. [ChangelogText]
 * maps these runs onto an annotated string so release notes never show the raw markers.
 */
internal data class MarkdownSpan(
    val text: String,
    val code: Boolean = false,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val strikethrough: Boolean = false,
    val url: String? = null,
)

/** A logical markdown block: a header, a bullet item or a paragraph, with soft-wrapped lines joined. */
internal data class ChangelogBlock(
    val text: String,
    val header: Boolean = false,
    val bullet: Boolean = false,
    val level: Int = 0,
)

/**
 * Matches the inline markdown GitHub release notes use, in priority order: `code` spans,
 * [markdown](links), **bold**, __bold__, ~~strikethrough~~, *italic*, _italic_ and bare http(s)
 * URLs. Asterisk and underscore emphasis follow the GFM flanking rules loosely: the delimiters may
 * not hug word characters, so identifiers like `intent_aware_ad_pivot` stay plain.
 */
private val INLINE_MARKDOWN = Regex(
    "`([^`\\n]+)`" +
        "|\\[([^\\]\\n]+)]\\(([^)\\n]+)\\)" +
        "|\\*\\*([^*\\n]+)\\*\\*" +
        "|__([^_\\n]+)__" +
        "|~~([^~\\n]+)~~" +
        "|(?<![\\w*])\\*(?!\\s)([^*\\n]+?)(?<=\\S)\\*(?![\\w*])" +
        "|(?<![\\w_])_(?!\\s)([^_\\n]+?)(?<=\\S)_(?![\\w_])" +
        "|https?://[^\\s<>\\[\\]()]+",
)

private val NUMERIC_ENTITY_REGEX = Regex("""&#([xX][0-9a-fA-F]+|[0-9]+);""")
private val HTML_TAG_REGEX = Regex("""<[^>]+>""")
private val HTML_COMMENT_REGEX = Regex("""<!--[\s\S]*?-->""")
private val HEADING_REGEX = Regex("""<h[1-6][^>]*>([\s\S]*?)</h[1-6]>""", RegexOption.IGNORE_CASE)
private val CODE_REGEX = Regex("""<code[^>]*>([\s\S]*?)</code>""", RegexOption.IGNORE_CASE)
private val BOLD_REGEX = Regex("""<(?:strong|b)[^>]*>([\s\S]*?)</(?:strong|b)>""", RegexOption.IGNORE_CASE)
private val ITALIC_REGEX = Regex("""<(?:em|i)[^>]*>([\s\S]*?)</(?:em|i)>""", RegexOption.IGNORE_CASE)
private val LINK_REGEX = Regex("""<a\s+[^>]*href="([^"]*)"[^>]*>([\s\S]*?)</a>""", RegexOption.IGNORE_CASE)
private val LIST_TOKEN_REGEX = Regex("""(</?(?:ul|ol|li)[^>]*>)""", RegexOption.IGNORE_CASE)
private val EMPTY_MIN_MGR_REGEX = Regex("""\s*\(minimum manager version:\s*(?:`{0,2}|<code[^>]*>\s*</code>)\s*\)""", RegexOption.IGNORE_CASE)
private val PARAGRAPH_REGEX = Regex("""</?p[^>]*>""", RegexOption.IGNORE_CASE)
private val BR_REGEX = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)

/**
 * Decodes XML/HTML character entities. Handles named entities, numeric entities, and double-escaping.
 */
internal fun unescapeHtml(text: String): String {
    if (!text.contains('&')) return text
    var res = text
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&#39;", "'")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
    if (res.contains("&#")) {
        res = NUMERIC_ENTITY_REGEX.replace(res) { match ->
            val value = match.groupValues[1]
            val codePoint = if (value.startsWith("x", ignoreCase = true)) {
                value.substring(1).toIntOrNull(16)
            } else {
                value.toIntOrNull()
            }
            if (codePoint != null && Character.isValidCodePoint(codePoint)) {
                String(Character.toChars(codePoint))
            } else {
                match.value
            }
        }
    }
    return res
}

/**
 * Cleans release notes that may contain raw HTML or HTML entities (e.g. from GitHub's releases.atom feed
 * or unauthenticated fallback) and converts them into standard GitHub-flavored Markdown.
 */
fun cleanChangelog(notes: String): String {
    if (notes.isBlank()) return ""
    var s = unescapeHtml(notes)
    if (s.contains("&lt;") || s.contains("&amp;")) {
        s = unescapeHtml(s)
    }

    if (!s.contains('<') && !s.contains("<!--")) {
        return s.trim()
    }

    s = HTML_COMMENT_REGEX.replace(s, "")
    s = EMPTY_MIN_MGR_REGEX.replace(s, "")
    s = HEADING_REGEX.replace(s) { "\n\n### ${it.groupValues[1]}\n\n" }
    s = CODE_REGEX.replace(s) { "`${it.groupValues[1]}`" }
    s = BOLD_REGEX.replace(s) { "**${it.groupValues[1]}**" }
    s = ITALIC_REGEX.replace(s) { "*${it.groupValues[1]}*" }
    s = LINK_REGEX.replace(s) { "[${it.groupValues[2]}](${it.groupValues[1]})" }

    // Preserve hierarchical list nesting with indentation
    val tokens = LIST_TOKEN_REGEX.split(s)
    val matches = LIST_TOKEN_REGEX.findAll(s).map { it.value }.toList()
    val sb = StringBuilder()
    var listLevel = 0
    for (i in tokens.indices) {
        sb.append(tokens[i])
        if (i < matches.size) {
            val tag = matches[i].lowercase()
            when {
                tag.startsWith("<ul") || tag.startsWith("<ol") -> {
                    listLevel++
                    sb.append("\n")
                }
                tag.startsWith("</ul") || tag.startsWith("</ol") -> {
                    listLevel = (listLevel - 1).coerceAtLeast(0)
                    sb.append("\n")
                }
                tag.startsWith("<li") -> {
                    val indent = "  ".repeat((listLevel - 1).coerceAtLeast(0))
                    sb.append("\n$indent- ")
                }
                tag.startsWith("</li") -> {
                    sb.append("\n")
                }
            }
        }
    }
    s = sb.toString()

    s = PARAGRAPH_REGEX.replace(s, "\n\n")
    s = BR_REGEX.replace(s, "\n")
    s = HTML_TAG_REGEX.replace(s, "")
    s = unescapeHtml(s)

    val cleanLines = mutableListOf<String>()
    for (line in s.lines()) {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed == "-") continue
        cleanLines += line.trimEnd()
    }
    return cleanLines.joinToString("\n")
}

/**
 * Groups the changelog into logical blocks: consecutive source lines in the same paragraph or
 * bullet item are joined with a space the way GitHub soft-wraps them, blank lines stay as block
 * separators, and headers are never merged with the line below. Automatically sanitizes HTML
 * and entities if present.
 */
internal fun changelogBlocks(notes: String): List<ChangelogBlock> {
    val clean = cleanChangelog(notes)
    val blocks = mutableListOf<ChangelogBlock>()
    for (rawLine in clean.lines()) {
        val trimmed = rawLine.trim()
        val indent = (rawLine.length - rawLine.trimStart().length) / 2
        val level = indent.coerceIn(0, 3)
        when {
            trimmed.isEmpty() -> blocks += ChangelogBlock("")
            trimmed.startsWith("### ") || trimmed.startsWith("## ") || trimmed.startsWith("# ") -> {
                val headerText = trimmed.removePrefix("### ").removePrefix("## ").removePrefix("# ")
                blocks += ChangelogBlock(headerText, header = true)
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") ->
                blocks += ChangelogBlock(trimmed.substring(2).trim(), bullet = true, level = level)
            else -> {
                val previous = blocks.lastOrNull()
                if (previous != null && !previous.header && previous.text.isNotEmpty()) {
                    blocks[blocks.lastIndex] = previous.copy(text = "${previous.text} $trimmed")
                } else {
                    blocks += ChangelogBlock(trimmed, level = level)
                }
            }
        }
    }
    return blocks
}

/**
 * Splits one line of inline markdown into styled runs. Styled content is parsed recursively so a
 * code span inside bold keeps both, while code spans themselves are taken verbatim.
 */
internal fun parseMarkdownSpans(text: String): List<MarkdownSpan> =
    parseInlineMarkdown(text, MarkdownSpan(""))

private fun parseInlineMarkdown(text: String, base: MarkdownSpan): List<MarkdownSpan> {
    val spans = mutableListOf<MarkdownSpan>()
    var index = 0
    for (match in INLINE_MARKDOWN.findAll(text)) {
        if (match.range.first > index) {
            spans += base.copy(text = text.substring(index, match.range.first))
        }
        when {
            match.groups[1] != null -> spans += base.copy(text = match.groupValues[1], code = true)
            match.groups[2] != null -> spans += base.copy(text = match.groupValues[2], url = match.groupValues[3])
            match.groups[4] != null -> spans += parseInlineMarkdown(match.groupValues[4], base.copy(bold = true))
            match.groups[5] != null -> spans += parseInlineMarkdown(match.groupValues[5], base.copy(bold = true))
            match.groups[6] != null -> spans += parseInlineMarkdown(match.groupValues[6], base.copy(strikethrough = true))
            match.groups[7] != null -> spans += parseInlineMarkdown(match.groupValues[7], base.copy(italic = true))
            match.groups[8] != null -> spans += parseInlineMarkdown(match.groupValues[8], base.copy(italic = true))
            else -> {
                // Bare URL: keep sentence punctuation out of the link itself.
                val raw = match.value
                val url = raw.trimEnd { it in ".,;:!?" }
                spans += base.copy(text = url, url = url)
                if (url.length < raw.length) {
                    spans += base.copy(text = raw.substring(url.length))
                }
            }
        }
        index = match.range.last + 1
    }
    if (index < text.length) {
        spans += base.copy(text = text.substring(index))
    }
    return spans.filter { it.text.isNotEmpty() }
}
