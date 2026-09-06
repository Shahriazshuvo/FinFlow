package com.finflow.feature.goals.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.finflow.feature.goals.presentation.GoalsRoute
import kotlinx.serialization.Serializable

/** Type-safe route for the Goals destination. */
@Serializable
data object GoalsRouteKey

fun NavController.navigateToGoals(navOptions: NavOptions? = null) =
    navigate(GoalsRouteKey, navOptions)

fun NavGraphBuilder.goalsScreen() {
    composable<GoalsRouteKey> {
        GoalsRoute()
    }
}
