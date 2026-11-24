package io.github.a092devs.nexfusion.updates

import java.util.Locale

/**
 * Pure preference-name helpers, kept separate from [AppSettings] so they can be unit tested
 * without an Android Context.
 */
object AppPreferenceNames {
    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    /** Design system options: AndroidX Material 3 and the official Miuix library. */
    const val THEME_STYLE_MATERIAL3 = "material3"
    const val THEME_STYLE_MIUIX = "miuix"

    /**
     * Historical value of the Miuix style. Early builds stored "miuix_glass"; it is migrated on
     * read so users keep their selected design system.
     */
    const val LEGACY_THEME_STYLE_MIUIX = "miuix_glass"

    /** The Miuix floating navigation bar is opt-in and disabled by default. */
    const val DEFAULT_FLOATING_NAVIGATION = false

    /** The Miuix floating navigation bar translucent liquid blur is disabled by default for optimal performance. */
    const val DEFAULT_FLOATING_BAR_TRANSLUCENT = false

    /** App language options: English or Simplified Chinese. */
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_SIMPLIFIED_CHINESE = "zh-CN"

    /** Preference order of the language dialog. */
    val LANGUAGES = listOf(
        LANGUAGE_ENGLISH,
        LANGUAGE_SIMPLIFIED_CHINESE,
    )
    val SUPPORTED_LANGUAGES = LANGUAGES

    /** Maps any stored language value to a supported one, defaulting to English or Chinese if system is zh. */
    fun normalizeLanguage(raw: String?): String = when (raw) {
        LANGUAGE_SIMPLIFIED_CHINESE -> LANGUAGE_SIMPLIFIED_CHINESE
        LANGUAGE_ENGLISH -> LANGUAGE_ENGLISH
        else -> if (Locale.getDefault().language.equals("zh", ignoreCase = true)) {
            LANGUAGE_SIMPLIFIED_CHINESE
        } else {
            LANGUAGE_ENGLISH
        }
    }

    /**
     * Color scheme options. Default is the refreshed cyan palette; remaining values
     * are fixed accent seeds applied consistently to both Material 3 and Miuix design systems.
     */
    const val COLOR_SCHEME_DEFAULT = "default"
    const val COLOR_SCHEME_SUNSET = "sunset"
    const val COLOR_SCHEME_EMERALD = "emerald"
    const val COLOR_SCHEME_ROSE = "rose"
    const val COLOR_SCHEME_AMBER = "amber"
    const val COLOR_SCHEME_INDIGO = "indigo"
    const val COLOR_SCHEME_FUCHSIA = "fuchsia"
    const val COLOR_SCHEME_LIME = "lime"

    /** Preference order of the colour-scheme dialog. */
    val COLOR_SCHEMES = listOf(
        COLOR_SCHEME_DEFAULT,
        COLOR_SCHEME_SUNSET,
        COLOR_SCHEME_EMERALD,
        COLOR_SCHEME_ROSE,
        COLOR_SCHEME_AMBER,
        COLOR_SCHEME_INDIGO,
        COLOR_SCHEME_FUCHSIA,
        COLOR_SCHEME_LIME,
    )

    /** Maps any stored colour-scheme value to a supported one. */
    fun normalizeColorScheme(raw: String?): String = when (raw) {
        "ocean", "sky" -> COLOR_SCHEME_DEFAULT
        "violet", "blurple" -> COLOR_SCHEME_INDIGO
        in COLOR_SCHEMES -> raw!!
        else -> COLOR_SCHEME_DEFAULT
    }

    /** Maps any stored theme-style value to a supported one. */
    fun normalizeStyle(raw: String?): String = when (raw) {
        THEME_STYLE_MIUIX, LEGACY_THEME_STYLE_MIUIX -> THEME_STYLE_MIUIX
        else -> THEME_STYLE_MATERIAL3
    }

    /** Maps any stored colour-mode value to a supported one. */
    fun normalizeTheme(raw: String?): String = when (raw) {
        THEME_LIGHT -> THEME_LIGHT
        THEME_DARK -> THEME_DARK
        else -> THEME_SYSTEM
    }

    fun isMiuix(style: String?): Boolean = normalizeStyle(style) == THEME_STYLE_MIUIX
}
