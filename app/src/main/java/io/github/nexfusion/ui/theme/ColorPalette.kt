package io.github.nexfusion.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Centralized color palette for NexFusion.
 * All semantic color seeds and fixed accent colors defined here.
 */
object ColorPalette {

    /** Default seed for the "Default" scheme — a distinctive deep indigo. */
    val DefaultFusionSeed = Color(0xFF3B3FD9)

    /** Fixed accent seeds for the named color schemes.
     * Each seed is a carefully tuned hue with balanced chroma for both light/dark modes.
     * Seeds are NOT pure hues — they're tuned for perceptual harmony in tonal palettes.
     */
    val VioletSeed = Color(0xFF6B46E0)
    val EmeraldSeed = Color(0xFF009666)
    val AmberSeed = Color(0xFFE8A000)
    val RoseSeed = Color(0xFFD63A6D)
    val OceanSeed = Color(0xFF0096A0)

    /** Miuix Light: Official Xiaomi/HyperOS palette */
    val MiuixLightPrimary = Color(0xFF0066FF)
    val MiuixLightOnPrimary = Color.White
    val MiuixLightPrimaryContainer = Color(0xFFE5EFFF)
    val MiuixLightOnPrimaryContainer = Color(0xFF003399)
    val MiuixLightSecondary = Color(0xFF4B6079)
    val MiuixLightSecondaryContainer = Color(0xFFE0EAF5)
    val MiuixLightOnSecondaryContainer = Color(0xFF10263E)
    val MiuixLightTertiary = Color(0xFF00897B)
    val MiuixLightTertiaryContainer = Color(0xFFCCF2ED)
    val MiuixLightBackground = Color(0xFFF4F6FB)
    val MiuixLightOnBackground = Color(0xFF1A1C22)
    val MiuixLightSurface = Color(0xFFFAFBFE)
    val MiuixLightOnSurface = Color(0xFF1A1C22)
    val MiuixLightSurfaceVariant = Color(0xFFE4E8F2)
    val MiuixLightOnSurfaceVariant = Color(0xFF444852)
    val MiuixLightSurfaceContainerLowest = Color(0xFFFFFFFF)
    val MiuixLightSurfaceContainerLow = Color(0xFFF7F9FC)
    val MiuixLightSurfaceContainer = Color(0xFFF0F4FA)
    val MiuixLightSurfaceContainerHigh = Color(0xFFE7EDF6)
    val MiuixLightSurfaceContainerHighest = Color(0xFFDDE4F0)
    val MiuixLightOutline = Color(0xFF6B7280)
    val MiuixLightOutlineVariant = Color(0xFFCBD2DE)

    /** Miuix Dark: Deep charcoal with cool blue undertones */
    val MiuixDarkPrimary = Color(0xFF3B82F6)
    val MiuixDarkOnPrimary = Color(0xFF00224E)
    val MiuixDarkPrimaryContainer = Color(0xFF1E3A8A)
    val MiuixDarkOnPrimaryContainer = Color(0xFFDBEAFE)
    val MiuixDarkSecondary = Color(0xFF94A3B8)
    val MiuixDarkSecondaryContainer = Color(0xFF243044)
    val MiuixDarkOnSecondaryContainer = Color(0xFFE2E8F0)
    val MiuixDarkTertiary = Color(0xFF2DD4BF)
    val MiuixDarkTertiaryContainer = Color(0xFF004D40)
    val MiuixDarkBackground = Color(0xFF0B0D14)
    val MiuixDarkOnBackground = Color(0xFFF1F5F9)
    val MiuixDarkSurface = Color(0xFF11141D)
    val MiuixDarkOnSurface = Color(0xFFF1F5F9)
    val MiuixDarkSurfaceVariant = Color(0xFF1E2330)
    val MiuixDarkOnSurfaceVariant = Color(0xFF94A3B8)
    val MiuixDarkSurfaceContainerLowest = Color(0xFF0E1119)
    val MiuixDarkSurfaceContainerLow = Color(0xFF141722)
    val MiuixDarkSurfaceContainer = Color(0xFF1A1F2C)
    val MiuixDarkSurfaceContainerHigh = Color(0xFF222838)
    val MiuixDarkSurfaceContainerHighest = Color(0xFF2C3448)
    val MiuixDarkOutline = Color(0xFF64748B)
    val MiuixDarkOutlineVariant = Color(0xFF334155)

    // Miuix Light scheme object for Theme.kt
    val MiuixLightScheme = lightColorScheme(
        primary = MiuixLightPrimary,
        onPrimary = MiuixLightOnPrimary,
        primaryContainer = MiuixLightPrimaryContainer,
        onPrimaryContainer = MiuixLightOnPrimaryContainer,
        secondary = MiuixLightSecondary,
        secondaryContainer = MiuixLightSecondaryContainer,
        onSecondaryContainer = MiuixLightOnSecondaryContainer,
        tertiary = MiuixLightTertiary,
        tertiaryContainer = MiuixLightTertiaryContainer,
        background = MiuixLightBackground,
        onBackground = MiuixLightOnBackground,
        surface = MiuixLightSurface,
        onSurface = MiuixLightOnSurface,
        surfaceVariant = MiuixLightSurfaceVariant,
        onSurfaceVariant = MiuixLightOnSurfaceVariant,
        surfaceContainerLowest = MiuixLightSurfaceContainerLowest,
        surfaceContainerLow = MiuixLightSurfaceContainerLow,
        surfaceContainer = MiuixLightSurfaceContainer,
        surfaceContainerHigh = MiuixLightSurfaceContainerHigh,
        surfaceContainerHighest = MiuixLightSurfaceContainerHighest,
        outline = MiuixLightOutline,
        outlineVariant = MiuixLightOutlineVariant,
    )

    // Miuix Dark scheme object for Theme.kt
    val MiuixDarkScheme = darkColorScheme(
        primary = MiuixDarkPrimary,
        onPrimary = MiuixDarkOnPrimary,
        primaryContainer = MiuixDarkPrimaryContainer,
        onPrimaryContainer = MiuixDarkOnPrimaryContainer,
        secondary = MiuixDarkSecondary,
        secondaryContainer = MiuixDarkSecondaryContainer,
        onSecondaryContainer = MiuixDarkOnSecondaryContainer,
        tertiary = MiuixDarkTertiary,
        tertiaryContainer = MiuixDarkTertiaryContainer,
        background = MiuixDarkBackground,
        onBackground = MiuixDarkOnBackground,
        surface = MiuixDarkSurface,
        onSurface = MiuixDarkOnSurface,
        surfaceVariant = MiuixDarkSurfaceVariant,
        onSurfaceVariant = MiuixDarkOnSurfaceVariant,
        surfaceContainerLowest = MiuixDarkSurfaceContainerLowest,
        surfaceContainerLow = MiuixDarkSurfaceContainerLow,
        surfaceContainer = MiuixDarkSurfaceContainer,
        surfaceContainerHigh = MiuixDarkSurfaceContainerHigh,
        surfaceContainerHighest = MiuixDarkSurfaceContainerHighest,
        outline = MiuixDarkOutline,
        outlineVariant = MiuixDarkOutlineVariant,
    )

    // Seekbar thumb alignment nudge
    val SeekbarThumbVerticalNudgeDp = 2.0f

    // Material 3 scheme generation from seed
    fun materialSchemeFromSeed(seed: Color, dark: Boolean): androidx.compose.material3.ColorScheme {
        val scheme = com.materialkolor.scheme.SchemeTonalSpot(
            sourceColorHct = com.materialkolor.hct.Hct.fromInt(seed.toArgb()),
            isDark = dark,
            contrastLevel = 0.0,
            specVersion = com.materialkolor.dynamiccolor.ColorSpec.SpecVersion.SPEC_2021,
            platform = com.materialkolor.scheme.DynamicScheme.Platform.PHONE,
        )
        return (if (dark) androidx.compose.material3.darkColorScheme() else androidx.compose.material3.lightColorScheme()).copy(
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

    // Scheme seed for Theme.kt
    fun schemeSeed(schemeName: String): Color = when (schemeName) {
        "violet" -> VioletSeed
        "emerald" -> EmeraldSeed
        "amber" -> AmberSeed
        "rose" -> RoseSeed
        "ocean" -> OceanSeed
        else -> DefaultFusionSeed
    }
}