package com.finflow.feature.dashboard.presentation

import androidx.compose.runtime.Composable
import com.finflow.feature.dashboard.presentation.screen.DashboardScreen

/**
 * Entry point for the Dashboard feature. Phase 1/2 ships the route and its place in the
 * navigation graph; the MVI contract, ViewModel and screen arrive in a later phase.
 */
@Composable
fun DashboardRoute() {
    DashboardScreen()
}
