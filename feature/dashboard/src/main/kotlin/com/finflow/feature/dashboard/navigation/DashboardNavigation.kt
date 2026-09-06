package com.finflow.feature.dashboard.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.finflow.feature.dashboard.presentation.DashboardRoute
import kotlinx.serialization.Serializable

/** Type-safe route for the Dashboard destination. */
@Serializable
data object DashboardRouteKey

fun NavController.navigateToDashboard(navOptions: NavOptions? = null) =
    navigate(DashboardRouteKey, navOptions)

fun NavGraphBuilder.dashboardScreen() {
    composable<DashboardRouteKey> {
        DashboardRoute()
    }
}
