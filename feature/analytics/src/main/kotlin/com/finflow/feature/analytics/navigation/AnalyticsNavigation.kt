package com.finflow.feature.analytics.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.finflow.feature.analytics.presentation.AnalyticsRoute
import kotlinx.serialization.Serializable

/** Type-safe route for the Analytics destination. */
@Serializable
data object AnalyticsRouteKey

fun NavController.navigateToAnalytics(navOptions: NavOptions? = null) =
    navigate(AnalyticsRouteKey, navOptions)

fun NavGraphBuilder.analyticsScreen() {
    composable<AnalyticsRouteKey> {
        AnalyticsRoute()
    }
}
