package com.finflow.feature.analytics.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.finflow.core.navigation.AnalyticsRouteKey
import com.finflow.feature.analytics.presentation.AnalyticsRoute

/** Registers the destination. The route key itself lives in `core:navigation`. */
fun NavGraphBuilder.analyticsScreen() {
    composable<AnalyticsRouteKey> {
        AnalyticsRoute()
    }
}
