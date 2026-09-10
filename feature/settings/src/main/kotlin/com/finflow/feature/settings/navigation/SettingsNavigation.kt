package com.finflow.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.finflow.core.navigation.SettingsRouteKey
import com.finflow.feature.settings.presentation.SettingsRoute

/** Registers the destination. The route key itself lives in `core:navigation`. */
fun NavGraphBuilder.settingsScreen() {
    composable<SettingsRouteKey> {
        SettingsRoute()
    }
}
