package io.github.nexfusion.ui

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Centralized design tokens shared between Material3 and Miuix implementations.
 * Provides consistent spacing, shapes, transitions, and component sizing across both design systems.
 */
object DesignTokens {

    // ========================================================================
    // Spacing Scale (4dp base unit)
    // ========================================================================
    val spaceXxs = 2.dp
    val spaceXs = 4.dp
    val spaceSm = 8.dp
    val spaceMd = 12.dp
    val spaceLg = 16.dp
    val spaceXl = 20.dp
    val spaceXxl = 24.dp
    val spaceXxxl = 32.dp

    // ========================================================================
    // Border Radius / Corner Radius
    // ========================================================================
    val radiusNone = 0.dp
    val radiusXs = 4.dp
    val radiusSm = 8.dp
    val radiusMd = 12.dp
    val radiusLg = 16.dp
    val radiusXl = 18.dp
    val radiusXxl = 24.dp
    val radiusFull = 9999.dp // Circle/pill shape

    // Predefined shapes
    val shapeNone = RoundedCornerShape(radiusNone)
    val shapeSmall = RoundedCornerShape(radiusSm)
    val shapeMedium = RoundedCornerShape(radiusMd)
    val shapeLarge = RoundedCornerShape(radiusLg)
    val shapeXLarge = RoundedCornerShape(radiusXl)
    val shapePill = RoundedCornerShape(percent = 50)

    // ========================================================================
    // Elevation / Shadow (Miuix uses blur, Material3 uses elevation)
    // ========================================================================
    val elevationLevel0 = 0.dp
    val elevationLevel1 = 1.dp
    val elevationLevel2 = 3.dp
    val elevationLevel3 = 6.dp
    val elevationLevel4 = 8.dp
    val elevationLevel5 = 12.dp

    // ========================================================================
    // Transitions & Animations - Duration & Easing (non-composable)
    // ========================================================================
    val durationFast = 150
    val durationNormal = 200
    val durationMedium = 250
    val durationSlow = 300
    val durationXSlow = 400

    val easingStandard = FastOutSlowInEasing
    val easingAccelerate = FastOutLinearInEasing
    val easingDecelerate = LinearOutSlowInEasing

    // Navigation-specific transitions (duration only)
    val navEnterAnim = durationMedium
    val navExitAnim = durationNormal
    val navPopEnterAnim = durationMedium
    val navPopExitAnim = durationNormal

    // Floating bar transitions (duration only)
    val floatingBarEnterAnim = 320
    val floatingBarExitAnim = 220
    val floatingBarContentEnterAnim = 220
    val floatingBarContentExitAnim = 160

    // ========================================================================
    // Typography Scale
    // ========================================================================
    val fontSizeDisplayLarge = 24.sp
    val fontSizeDisplayMedium = 20.sp
    val fontSizeDisplaySmall = 18.sp
    val fontSizeHeadlineLarge = 16.sp
    val fontSizeHeadlineMedium = 14.sp
    val fontSizeHeadlineSmall = 12.sp
    val fontSizeTitleLarge = 14.sp
    val fontSizeTitleMedium = 12.sp
    val fontSizeTitleSmall = 11.sp
    val fontSizeBodyLarge = 16.sp
    val fontSizeBodyMedium = 14.sp
    val fontSizeBodySmall = 12.sp
    val fontSizeLabelLarge = 12.sp
    val fontSizeLabelMedium = 11.sp
    val fontSizeLabelSmall = 10.sp

    val fontWeightLight = 300
    val fontWeightNormal = 400
    val fontWeightMedium = 500
    val fontWeightSemiBold = 600
    val fontWeightBold = 700

    // ========================================================================
    // Component Sizing
    // ========================================================================
    val iconSizeXs = 16.dp
    val iconSizeSm = 20.dp
    val iconSizeMd = 24.dp
    val iconSizeLg = 28.dp
    val iconSizeXl = 32.dp

    val touchTargetMin = 48.dp // Minimum touch target per Material guidelines
    val touchTargetComfortable = 56.dp

    // Navigation bar
    val navBarHeight = 84.dp
    val navBarItemWidth = 92.dp
    val navBarItemHeight = 64.dp
    val floatingNavItemWidth = 92.dp
    val floatingNavItemHeight = 64.dp
    val floatingBarBottomPadding = 12.dp

    // Card padding
    val cardPaddingSm = 12.dp
    val cardPaddingMd = 16.dp
    val cardPaddingLg = 18.dp
    val cardPaddingXl = 24.dp

    // Content padding
    val contentPaddingHorizontal = 16.dp
    val contentPaddingVertical = 8.dp
    val sectionSpacing = 24.dp

    // ========================================================================
    // Z-Index Layers
    // ========================================================================
    val zIndexBase = 0f
    val zIndexContent = 1f
    val zIndexCard = 10f
    val zIndexModal = 100f
    val zIndexSnackbar = 200f
    val zIndexNavBar = 300f
    val zIndexFloatingNav = 400f
    val zIndexDialog = 500f
    val zIndexToast = 600f

    // ========================================================================
    // Opacity Values
    // ========================================================================
    val opacityDisabled = 0.38f
    val opacityHover = 0.04f
    val opacityFocus = 0.12f
    val opacityPress = 0.16f
    val opacityDragged = 0.20f
    val opacityScrim = 0.50f
    val opacityBackdrop = 0.32f

    // Floating bar specific
    val floatingBarTranslucentOpacity = 0.82f
    val floatingBarPillOpacity = 0.22f // Dark, 0.14f for light
    val floatingBarIconAlphaSelected = 1f
    val floatingBarIconAlphaUnselected = 0.65f
    val floatingBarIconAlphaPressed = 0.85f

    // ========================================================================
    // Border / Divider
    // ========================================================================
    val borderWidthThin = 1.dp
    val borderWidthMedium = 2.dp
    val dividerThickness = 1.dp

    // ========================================================================
    // Breakpoints (for responsive layouts)
    // ========================================================================
    val breakpointSm = 600.dp
    val breakpointMd = 840.dp
    val breakpointLg = 1200.dp
}

/**
 * Design system detection - resolved via composition local
 */
val LocalIsMiuix = compositionLocalOf { false }

@Composable
fun isMiuix(): Boolean = LocalIsMiuix.current

@Composable
fun ProvideDesignSystem(
    isMiuix: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalIsMiuix provides isMiuix) {
        content()
    }
}

/**
 * Shape accessors for composable context
 */
@Composable
fun cardShape(): androidx.compose.foundation.shape.RoundedCornerShape = DesignTokens.shapeLarge

@Composable
fun actionCardShape(): androidx.compose.foundation.shape.RoundedCornerShape = DesignTokens.shapeXLarge

@Composable
fun bottomBarHeight(): androidx.compose.ui.unit.Dp = DesignTokens.navBarHeight

@Composable
fun floatingBarTranslucentOpacity(): Float = DesignTokens.floatingBarTranslucentOpacity