package com.finflow.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// FinFlow's identity is a deep "money" green (#1B6B4A) with an amber secondary. The
// schemes below are written out explicitly rather than generated from a seed at runtime so
// the app looks identical on every device and in every screenshot.

internal val FinFlowLightColorScheme = lightColorScheme(
    primary = Color(0xFF1B6B4A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA7F2CB),
    onPrimaryContainer = Color(0xFF002114),
    inversePrimary = Color(0xFF8BD6AF),
    secondary = Color(0xFF7A5900),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDEA6),
    onSecondaryContainer = Color(0xFF261A00),
    tertiary = Color(0xFF3B6470),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBFEAF9),
    onTertiaryContainer = Color(0xFF001F27),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBFDF8),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFBFDF8),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDCE5DD),
    onSurfaceVariant = Color(0xFF404943),
    surfaceTint = Color(0xFF1B6B4A),
    inverseSurface = Color(0xFF2E312F),
    inverseOnSurface = Color(0xFFEFF1ED),
    outline = Color(0xFF707973),
    outlineVariant = Color(0xFFC0C9C2),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFBFDF8),
    surfaceDim = Color(0xFFDBDED9),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F7F2),
    surfaceContainer = Color(0xFFEFF1ED),
    surfaceContainerHigh = Color(0xFFE9EBE7),
    surfaceContainerHighest = Color(0xFFE3E5E1),
)

internal val FinFlowDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8BD6AF),
    onPrimary = Color(0xFF003825),
    primaryContainer = Color(0xFF005236),
    onPrimaryContainer = Color(0xFFA7F2CB),
    inversePrimary = Color(0xFF1B6B4A),
    secondary = Color(0xFFEBC078),
    onSecondary = Color(0xFF412D00),
    secondaryContainer = Color(0xFF5D4200),
    onSecondaryContainer = Color(0xFFFFDEA6),
    tertiary = Color(0xFFA4CDDC),
    onTertiary = Color(0xFF063542),
    tertiaryContainer = Color(0xFF234C58),
    onTertiaryContainer = Color(0xFFBFEAF9),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF191C1A),
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = Color(0xFF404943),
    onSurfaceVariant = Color(0xFFC0C9C2),
    surfaceTint = Color(0xFF8BD6AF),
    inverseSurface = Color(0xFFE1E3DF),
    inverseOnSurface = Color(0xFF2E312F),
    outline = Color(0xFF8A938C),
    outlineVariant = Color(0xFF404943),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF353836),
    surfaceDim = Color(0xFF111412),
    surfaceContainerLowest = Color(0xFF0C0F0D),
    surfaceContainerLow = Color(0xFF191C1A),
    surfaceContainer = Color(0xFF1D201E),
    surfaceContainerHigh = Color(0xFF272B29),
    surfaceContainerHighest = Color(0xFF323633),
)

/**
 * Semantic colours Material 3 has no slot for. Money direction is the core visual
 * language of the app, so income/expense get first-class tokens rather than being
 * hard-coded green/red at each call site.
 */
@Immutable
data class FinFlowExtendedColors(
    val income: Color,
    val onIncome: Color,
    val incomeContainer: Color,
    val expense: Color,
    val onExpense: Color,
    val expenseContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val success: Color,
    val neutral: Color,
)

internal val LightExtendedColors = FinFlowExtendedColors(
    income = Color(0xFF2E7D32),
    onIncome = Color(0xFFFFFFFF),
    incomeContainer = Color(0xFFC8E6C9),
    expense = Color(0xFFC62828),
    onExpense = Color(0xFFFFFFFF),
    expenseContainer = Color(0xFFFFCDD2),
    warning = Color(0xFFB26A00),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFE0B2),
    success = Color(0xFF2E7D32),
    neutral = Color(0xFF707973),
)

internal val DarkExtendedColors = FinFlowExtendedColors(
    income = Color(0xFF7FD68B),
    onIncome = Color(0xFF00390C),
    incomeContainer = Color(0xFF1B5E20),
    expense = Color(0xFFFFB4AB),
    onExpense = Color(0xFF690005),
    expenseContainer = Color(0xFF8C1D18),
    warning = Color(0xFFFFB95C),
    onWarning = Color(0xFF452B00),
    warningContainer = Color(0xFF674100),
    success = Color(0xFF7FD68B),
    neutral = Color(0xFF8A938C),
)

val LocalFinFlowColors = staticCompositionLocalOf { LightExtendedColors }
