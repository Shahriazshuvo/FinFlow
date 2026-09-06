package com.finflow.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.finflow.feature.settings.presentation.SettingsRoute
import kotlinx.serialization.Serializable

/** Type-safe route for the Settings destination. */
@Serializable
data object SettingsRouteKey

fun NavController.navigateToSettings(navOptions: NavOptions? = null) =
    navigate(SettingsRouteKey, navOptions)

fun NavGraphBuilder.settingsScreen() {
    composable<SettingsRouteKey> {
        SettingsRoute()
    }
}
