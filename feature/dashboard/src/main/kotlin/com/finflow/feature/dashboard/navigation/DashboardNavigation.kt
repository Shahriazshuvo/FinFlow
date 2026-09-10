package com.finflow.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.finflow.core.navigation.DashboardRouteKey
import com.finflow.feature.dashboard.presentation.DashboardRoute

/** Registers the destination. The route key itself lives in `core:navigation`. */
fun NavGraphBuilder.dashboardScreen() {
    composable<DashboardRouteKey> {
        DashboardRoute()
    }
}
