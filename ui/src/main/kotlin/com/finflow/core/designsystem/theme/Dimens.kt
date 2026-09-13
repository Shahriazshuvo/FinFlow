package com.finflow.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Every non-spacing dimension in the app: icon sizes, stroke widths, corner radii,
 * component heights, elevations and alphas.
 *
 * Rule for the whole codebase: **no literal `.dp` / alpha value anywhere outside this
 * file.** Components read them as `FinFlowTheme.dimens.iconMd`, padding comes from
 * `FinFlowTheme.spacing.lg`. That keeps the visual language in one reviewable place and
 * makes a global size change a one-line edit instead of a grep-and-pray.
 */
@Immutable
data class Dimens(
    // Icons
    val iconXs: Dp = 12.dp,
    val iconSm: Dp = 18.dp,
    val iconMd: Dp = 24.dp,
    val iconLg: Dp = 32.dp,
    val iconXl: Dp = 48.dp,
    val iconXxl: Dp = 64.dp,

    // Strokes, borders and dividers
    val strokeThin: Dp = 1.dp,
    val strokeMedium: Dp = 2.dp,
    val strokeThick: Dp = 4.dp,
    val dividerThickness: Dp = 1.dp,

    // Corner radii
    val cornerXs: Dp = 4.dp,
    val cornerSm: Dp = 8.dp,
    val cornerMd: Dp = 12.dp,
    val cornerLg: Dp = 16.dp,
    val cornerXl: Dp = 24.dp,
    val cornerFull: Dp = 999.dp,

    // Elevation
    val elevationNone: Dp = 0.dp,
    val elevationLow: Dp = 1.dp,
    val elevationMedium: Dp = 3.dp,
    val elevationHigh: Dp = 6.dp,

    // Component sizes
    val buttonHeight: Dp = 56.dp,
    val textFieldHeight: Dp = 56.dp,
    val listItemHeight: Dp = 64.dp,
    val topAppBarHeight: Dp = 64.dp,
    val bottomBarHeight: Dp = 80.dp,
    val fabSize: Dp = 56.dp,
    val chipHeight: Dp = 32.dp,
    val avatarSm: Dp = 32.dp,
    val avatarMd: Dp = 40.dp,
    val avatarLg: Dp = 56.dp,

    // Progress indicators
    val progressHeight: Dp = 8.dp,
    val progressHeightLarge: Dp = 12.dp,
    val progressIndicatorSm: Dp = 18.dp,
    val progressIndicatorMd: Dp = 32.dp,
    val progressIndicatorLg: Dp = 48.dp,

    // Layout constraints
    val maxContentWidth: Dp = 600.dp,
    val sheetHandleWidth: Dp = 32.dp,
    val emptyStateIconSize: Dp = 64.dp,
    val chartHeight: Dp = 220.dp,
    val chartBarWidth: Dp = 24.dp,
)

/** Opacity values, kept here for the same reason as the sizes above. */
@Immutable
data class Alphas(
    val disabled: Float = 0.38f,
    val medium: Float = 0.60f,
    val subtle: Float = 0.74f,
    val full: Float = 1f,
    val scrim: Float = 0.32f,
    val surfaceTint: Float = 0.12f,
)

/** Animation durations in milliseconds. */
@Immutable
data class Durations(
    val short: Int = 150,
    val medium: Int = 250,
    val long: Int = 400,
)

val LocalDimens = staticCompositionLocalOf { Dimens() }
val LocalAlphas = staticCompositionLocalOf { Alphas() }
val LocalDurations = staticCompositionLocalOf { Durations() }