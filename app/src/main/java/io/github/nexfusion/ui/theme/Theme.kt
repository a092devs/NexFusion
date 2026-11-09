package io.github.nexfusion.ui.theme

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.hct.Hct
import com.materialkolor.scheme.DynamicScheme
import com.materialkolor.scheme.SchemeTonalSpot
import io.github.nexfusion.updates.AppPreferenceNames
import io.github.nexfusion.updates.AppSettings
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController


enum class ThemeStyle {
    MATERIAL3,
    MIUIX,
}

val LocalThemeStyle = staticCompositionLocalOf { ThemeStyle.MATERIAL3 }

val isMiuixStyle: Boolean
    @Composable
    get() = LocalThemeStyle.current == ThemeStyle.MIUIX

/**
 * Configuration parameters for NexFusion's adaptive visual theme.
 */
data class NexFusionThemeConfig(
    /** True while the official Miuix design system is active. */
    val isMiuix: Boolean = false,
    val cardShape: RoundedCornerShape = RoundedCornerShape(16.dp),
    val cardBorder: BorderStroke? = null,
)

val LocalNexFusionThemeConfig = staticCompositionLocalOf { NexFusionThemeConfig() }

@Composable
fun nexFusionCardShape(): RoundedCornerShape = LocalNexFusionThemeConfig.current.cardShape

@Composable
fun nexFusionCardBorder(): BorderStroke? = LocalNexFusionThemeConfig.current.cardBorder

@Composable
fun nexFusionCardColors(
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
): CardColors {
    val isMiuix = LocalNexFusionThemeConfig.current.isMiuix
    val actualContainer = if (isMiuix) {
        containerColor.copy(alpha = 0.82f)
    } else {
        containerColor
    }
    return CardDefaults.cardColors(
        containerColor = actualContainer,
        contentColor = contentColor,
    )
}

// ============================================================================
// NEXFUSION COLOR SYSTEM - Sophisticated Harmonious Palettes
// ============================================================================
// Each seed is carefully chosen for perceptual harmony, accessibility, and
// brand coherence. Tonal palettes generated via Material 3's tonal spot algorithm
// ensure consistent contrast across light/dark and both design systems.
// ============================================================================

/**
 * Default seed for the "Default" scheme — a distinctive deep indigo.
 * This is the brand seed used when no named scheme is selected.
 */
val DefaultFusionSeed = Color(0xFF3B3FD9)

/** Fixed accent seeds for the named color schemes.
 * Each seed is a carefully tuned hue with balanced chroma for both light/dark modes.
 * Seeds are NOT pure hues — they're tuned for perceptual harmony in tonal palettes.
 */
private fun schemeSeed(colorScheme: String): Color = when (colorScheme) {
    // DEEP INDIGO VIOLET — Rich, mysterious, premium. High chroma but not garish.
    AppPreferenceNames.COLOR_SCHEME_VIOLET -> Color(0xFF6B46E0)
    // VERDANT EMERALD — Natural, vibrant, alive. Slightly blue-shifted for depth.
    AppPreferenceNames.COLOR_SCHEME_EMERALD -> Color(0xFF009666)
    // GOLDEN AMBER — Warm, energetic, optimistic. Not too yellow, not too orange.
    AppPreferenceNames.COLOR_SCHEME_AMBER -> Color(0xFFE8A000)
    // ROSE QUARTZ — Soft, sophisticated, modern. Red-violet balance, not pink.
    AppPreferenceNames.COLOR_SCHEME_ROSE -> Color(0xFFD63A6D)
    // DEEP TEAL OCEAN — Calm, tech-forward, trustworthy. Blue-green balance.
    AppPreferenceNames.COLOR_SCHEME_OCEAN -> Color(0xFF0096A0)
    else -> DefaultFusionSeed
}

/** Material 3 schemes generated from seeds using tonal spot algorithm.
 * These provide the base M3 schemes that Miuix then harmonizes with.
 */
private fun materialSchemeFromSeed(seed: Color, dark: Boolean): ColorScheme {
    val scheme = SchemeTonalSpot(
        sourceColorHct = Hct.fromInt(seed.toArgb()),
        isDark = dark,
        contrastLevel = 0.0,
        specVersion = ColorSpec.SpecVersion.SPEC_2021,
        platform = DynamicScheme.Platform.PHONE,
    )
    return (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = Color(scheme.primary),
        onPrimary = Color(scheme.onPrimary),
        primaryContainer = Color(scheme.primaryContainer),
        onPrimaryContainer = Color(scheme.onPrimaryContainer),
        secondary = Color(scheme.secondary),
        onSecondary = Color(scheme.onSecondary),
        secondaryContainer = Color(scheme.secondaryContainer),
        onSecondaryContainer = Color(scheme.onSecondaryContainer),
        tertiary = Color(scheme.tertiary),
        onTertiary = Color(scheme.onTertiary),
        tertiaryContainer = Color(scheme.tertiaryContainer),
        onTertiaryContainer = Color(scheme.onTertiaryContainer),
        error = Color(scheme.error),
        onError = Color(scheme.onError),
        errorContainer = Color(scheme.errorContainer),
        onErrorContainer = Color(scheme.onErrorContainer),
        background = Color(scheme.background),
        onBackground = Color(scheme.onBackground),
        surface = Color(scheme.surface),
        onSurface = Color(scheme.onSurface),
        surfaceVariant = Color(scheme.surfaceVariant),
        onSurfaceVariant = Color(scheme.onSurfaceVariant),
        surfaceContainerLowest = Color(scheme.surfaceContainerLowest),
        surfaceContainerLow = Color(scheme.surfaceContainerLow),
        surfaceContainer = Color(scheme.surfaceContainer),
        surfaceContainerHigh = Color(scheme.surfaceContainerHigh),
        surfaceContainerHighest = Color(scheme.surfaceContainerHighest),
        outline = Color(scheme.outline),
        outlineVariant = Color(scheme.outlineVariant),
        inverseSurface = Color(scheme.inverseSurface),
        inverseOnSurface = Color(scheme.inverseOnSurface),
        inversePrimary = Color(scheme.inversePrimary),
    )
}

/**
 * Miuix Light: Official Xiaomi/HyperOS palette — clean, airy, slightly cool blue bias.
 * Primary: Xiaomi Blue (0066FF). Surfaces: cool white with subtle blue tint.
 */
private val MiuixLightScheme = lightColorScheme(
    primary = Color(0xFF0066FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5EFFF),
    onPrimaryContainer = Color(0xFF003399),
    secondary = Color(0xFF4B6079),
    secondaryContainer = Color(0xFFE0EAF5),
    onSecondaryContainer = Color(0xFF10263E),
    tertiary = Color(0xFF00897B),
    tertiaryContainer = Color(0xFFCCF2ED),
    background = Color(0xFFF4F6FB),
    onBackground = Color(0xFF1A1C22),
    surface = Color(0xFFFAFBFE),
    onSurface = Color(0xFF1A1C22),
    surfaceVariant = Color(0xFFE4E8F2),
    onSurfaceVariant = Color(0xFF444852),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F9FC),
    surfaceContainer = Color(0xFFF0F4FA),
    surfaceContainerHigh = Color(0xFFE7EDF6),
    surfaceContainerHighest = Color(0xFFDDE4F0),
    outline = Color(0xFF6B7280),
    outlineVariant = Color(0xFFCBD2DE),
)

/**
 * Miuix Dark: Deep charcoal with cool blue undertones — not pure black.
 * Primary: Bright Xiaomi Blue (3B82F6). Surfaces: layered charcoal blues.
 */
private val MiuixDarkScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color(0xFF00224E),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF94A3B8),
    secondaryContainer = Color(0xFF243044),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFF2DD4BF),
    tertiaryContainer = Color(0xFF004D40),
    background = Color(0xFF0B0D14),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF11141D),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E2330),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerLowest = Color(0xFF0E1119),
    surfaceContainerLow = Color(0xFF141722),
    surfaceContainer = Color(0xFF1A1F2C),
    surfaceContainerHigh = Color(0xFF222838),
    surfaceContainerHighest = Color(0xFF2C3448),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF334155),
)

/**
 * NexFusion adaptive dual-theme system supporting both standard AndroidX Material 3
 * and official Miuix (Xiaomi HyperOS) design system with squircle and blur support.
 *
 * [colorSchemeRaw] selects the accent palette: "default" keeps each design system's own
 * palette, while a named scheme ("violet", "emerald", "amber", "rose", "ocean") applies a
 * fixed seed to both design systems.
 */
@Composable
fun NexFusionTheme(
    themeModeRaw: String = AppSettings.THEME_SYSTEM,
    themeStyle: String = AppSettings.THEME_STYLE_MATERIAL3,
    colorSchemeRaw: String = AppSettings.COLOR_SCHEME_DEFAULT,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val themeMode = AppPreferenceNames.normalizeTheme(themeModeRaw)
    val colorScheme = AppPreferenceNames.normalizeColorScheme(colorSchemeRaw)
    val isDark = when (themeMode) {
        AppSettings.THEME_LIGHT -> false
        AppSettings.THEME_DARK -> true
        else -> systemDark
    }

    val style = if (AppPreferenceNames.isMiuix(themeStyle)) ThemeStyle.MIUIX else ThemeStyle.MATERIAL3
    val context = LocalContext.current
    val accent = schemeSeed(colorScheme)

    if (style == ThemeStyle.MIUIX) {
        val colorSchemeMode = if (isDark) ColorSchemeMode.MonetDark else ColorSchemeMode.MonetLight
        val controller = remember(colorSchemeMode, accent, isDark) {
            ThemeController(
                colorSchemeMode = colorSchemeMode,
                keyColor = accent,
                isDark = isDark,
            )
        }
        val m3Equivalent = materialSchemeFromSeed(accent, isDark)

        val themeConfig = NexFusionThemeConfig(
            isMiuix = true,
            cardShape = RoundedCornerShape(22.dp),
            cardBorder = null,
        )

        CompositionLocalProvider(
            LocalThemeStyle provides ThemeStyle.MIUIX,
            LocalNexFusionThemeConfig provides themeConfig,
        ) {
            MiuixTheme(controller = controller) {
                MaterialTheme(colorScheme = m3Equivalent) {
                    // Miuix components only provide their own content colour, so Material
                    // building blocks in this branch (bare Text/Icon without an explicit
                    // colour) would otherwise fall back to Color.Black - unreadable on the
                    // dark Miuix surfaces. Keep AndroidX's content colour aligned with the
                    // Material role equivalent used while Miuix is active.
                    CompositionLocalProvider(
                        LocalContentColor provides m3Equivalent.onBackground,
                    ) {
                        content()
                    }
                }
            }
        }
    } else {
        val m3Colors = materialSchemeFromSeed(accent, isDark)

        val themeConfig = NexFusionThemeConfig(
            isMiuix = false,
            cardShape = RoundedCornerShape(16.dp),
            cardBorder = null,
        )

        CompositionLocalProvider(
            LocalThemeStyle provides ThemeStyle.MATERIAL3,
            LocalNexFusionThemeConfig provides themeConfig,
        ) {
            MaterialTheme(colorScheme = m3Colors, content = content)
        }
    }
}
