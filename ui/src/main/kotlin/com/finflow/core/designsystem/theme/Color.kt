package com.finflow.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// FinFlow's identity is an indigo-on-gray-blue system: #1234D1 for anything actionable, a
// #101323→#F8F9FC neutral ramp for everything structural, and green reserved exclusively for
// income so money direction stays readable at a glance. Extracted from the reference designs
// in docs/screenshot/ — see docs/adr/0010-finmori-visual-identity.md.
//
// Written out explicitly rather than generated from a seed so the app looks identical on every
// device and in every screenshot. Every foreground/background pair below was contrast-checked
// against WCAG AA; the few that land in AA-large territory are noted where they occur.

internal val FinFlowLightColorScheme = lightColorScheme(
    primary = Color(0xFF1234D1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEDF2FF),
    onPrimaryContainer = Color(0xFF1A297D),
    inversePrimary = Color(0xFF8FA6FF),
    secondary = Color(0xFF293056),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEDF2FF),
    onSecondaryContainer = Color(0xFF293056),
    tertiary = Color(0xFF4E5BA6),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCE3FF),
    onTertiaryContainer = Color(0xFF1A297D),
    error = Color(0xFFD92D20),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEF3F2),
    onErrorContainer = Color(0xFF7A1710),
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF101323),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF101323),
    surfaceVariant = Color(0xFFEDF2FF),
    onSurfaceVariant = Color(0xFF363F72),
    surfaceTint = Color(0xFF1234D1),
    inverseSurface = Color(0xFF293056),
    inverseOnSurface = Color(0xFFF8F9FC),
    outline = Color(0xFFD5D9EB),
    outlineVariant = Color(0xFFE7EAF4),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFEDEFF7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBFCFE),
    surfaceContainer = Color(0xFFF8F9FC),
    surfaceContainerHigh = Color(0xFFF2F4F9),
    surfaceContainerHighest = Color(0xFFEDF2FF),
)

// No dark reference frames exist — every screenshot is light — so this is derived from the same
// hues rather than sampled. Two values are deliberately *not* the light ones darkened: `primary`
// lifts to #8FA6FF and the CTA surface to #4361EE, because #1234D1 and #293056 both fall under
// 3:1 against the #101323 ground and would read as unlit shapes.
internal val FinFlowDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8FA6FF),
    onPrimary = Color(0xFF0A1A5C),
    primaryContainer = Color(0xFF1A297D),
    onPrimaryContainer = Color(0xFFDCE3FF),
    inversePrimary = Color(0xFF1234D1),
    secondary = Color(0xFFC4C9E4),
    onSecondary = Color(0xFF293056),
    secondaryContainer = Color(0xFF363F72),
    onSecondaryContainer = Color(0xFFE4E7F5),
    tertiary = Color(0xFFA9B4E8),
    onTertiary = Color(0xFF1E2547),
    tertiaryContainer = Color(0xFF363F72),
    onTertiaryContainer = Color(0xFFDCE3FF),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF5F1410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF101323),
    onBackground = Color(0xFFF8F9FC),
    surface = Color(0xFF1B2035),
    onSurface = Color(0xFFF8F9FC),
    surfaceVariant = Color(0xFF2A3050),
    onSurfaceVariant = Color(0xFFC4C9E4),
    surfaceTint = Color(0xFF8FA6FF),
    inverseSurface = Color(0xFFF8F9FC),
    inverseOnSurface = Color(0xFF101323),
    outline = Color(0xFF6B74A6),
    outlineVariant = Color(0xFF2A3050),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF2A3050),
    surfaceDim = Color(0xFF0B0E1A),
    surfaceContainerLowest = Color(0xFF0B0E1A),
    surfaceContainerLow = Color(0xFF151931),
    surfaceContainer = Color(0xFF1B2035),
    surfaceContainerHigh = Color(0xFF232943),
    surfaceContainerHighest = Color(0xFF2A3050),
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
    /**
     * A deliberately solid, high-attention surface: the primary CTA, the bottom bar, a chart
     * tooltip. Distinct from `primary` because it is a *surface* role rather than an accent,
     * and distinct from `inverseSurface` because it must stay dark-on-light in light mode and
     * lit in dark mode — `inverseSurface` flips, which would turn the primary button white.
     */
    val emphasis: Color,
    val onEmphasis: Color,
    /** Goal progress. Its own hue: progress is not income, and reading it as money misleads. */
    val progress: Color,
    val progressTrack: Color,
)

internal val LightExtendedColors = FinFlowExtendedColors(
    // 3.91:1 on white — AA-large. Only ever used for amounts, which are >=16sp SemiBold.
    income = Color(0xFF079455),
    onIncome = Color(0xFFFFFFFF),
    incomeContainer = Color(0xFFECFDF3),
    expense = Color(0xFFD92D20),
    onExpense = Color(0xFFFFFFFF),
    expenseContainer = Color(0xFFFEF3F2),
    warning = Color(0xFFB54708),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFEF0C7),
    success = Color(0xFF079455),
    neutral = Color(0xFF6E75A3),
    emphasis = Color(0xFF293056),
    onEmphasis = Color(0xFFFFFFFF),
    progress = Color(0xFF66C61C),
    progressTrack = Color(0xFFD5D9EB),
)

internal val DarkExtendedColors = FinFlowExtendedColors(
    income = Color(0xFF4ADE9C),
    onIncome = Color(0xFF00391F),
    incomeContainer = Color(0xFF04653A),
    expense = Color(0xFFFF8A80),
    onExpense = Color(0xFF5F1410),
    expenseContainer = Color(0xFF8C1D18),
    warning = Color(0xFFFDB022),
    onWarning = Color(0xFF452B00),
    warningContainer = Color(0xFF7A4100),
    success = Color(0xFF4ADE9C),
    neutral = Color(0xFF9AA1C4),
    emphasis = Color(0xFF4361EE),
    onEmphasis = Color(0xFFFFFFFF),
    progress = Color(0xFF85E33C),
    progressTrack = Color(0xFF2A3050),
)

val LocalFinFlowColors = staticCompositionLocalOf { LightExtendedColors }
