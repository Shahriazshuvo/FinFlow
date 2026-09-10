package com.finflow.feature.goals.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.finflow.core.navigation.GoalsRouteKey
import com.finflow.feature.goals.presentation.GoalsRoute

/** Registers the destination. The route key itself lives in `core:navigation`. */
fun NavGraphBuilder.goalsScreen() {
    composable<GoalsRouteKey> {
        GoalsRoute()
    }
}
