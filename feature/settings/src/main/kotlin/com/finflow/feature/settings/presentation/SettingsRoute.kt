package com.finflow.feature.settings.presentation

import androidx.compose.runtime.Composable
import com.finflow.feature.settings.presentation.screen.SettingsScreen

/**
 * Entry point for the Settings feature. Phase 1/2 ships the route and its place in the
 * navigation graph; the MVI contract, ViewModel and screen arrive in a later phase.
 */
@Composable
fun SettingsRoute() {
    SettingsScreen()
}
