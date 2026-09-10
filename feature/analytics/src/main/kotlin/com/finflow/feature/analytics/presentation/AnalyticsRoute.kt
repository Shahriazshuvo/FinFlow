package com.finflow.feature.analytics.presentation

import androidx.compose.runtime.Composable
import com.finflow.feature.analytics.presentation.screen.AnalyticsScreen

/**
 * Entry point for the Analytics feature. Phase 1/2 ships the route and its place in the
 * navigation graph; the MVI contract, ViewModel and screen arrive in a later phase.
 */
@Composable
fun AnalyticsRoute() {
    AnalyticsScreen()
}
