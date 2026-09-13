package com.finflow.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.font.FontFamily

/**
 * Dynamic colour is deliberately off: FinFlow's indigo identity is part of the product, and
 * consistent screenshots matter more here than wallpaper matching.
 */
@Composable
fun FinFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) FinFlowDarkColorScheme else FinFlowLightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalFinFlowColors provides extendedColors,
        LocalSpacing provides Spacing(),
        LocalDimens provides Dimens(),
        LocalAlphas provides Alphas(),
        LocalDurations provides Durations(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FinFlowTypography,
            content = content,
        )
    }
}

/**
 * Design token accessors. Everything visual is read through these — `FinFlowTheme.dimens`,
 * `FinFlowTheme.spacing`, `FinFlowTheme.colors` — so no literal size, alpha or duration
 * appears outside the theme package.
 */
object FinFlowTheme {
    val colors: FinFlowExtendedColors
        @Composable @ReadOnlyComposable get() = LocalFinFlowColors.current

    /**
     * The display face, for hero numerals and proper names. Pair it with an existing size
     * rather than a size of its own:
     * `MaterialTheme.typography.headlineLarge.copy(fontFamily = FinFlowTheme.serif)`.
     */
    val serif: FontFamily get() = FinFlowSerifFamily

    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current

    val dimens: Dimens
        @Composable @ReadOnlyComposable get() = LocalDimens.current

    val alphas: Alphas
        @Composable @ReadOnlyComposable get() = LocalAlphas.current

    val durations: Durations
        @Composable @ReadOnlyComposable get() = LocalDurations.current
}