package com.finflow.feature.budgets.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.finflow.feature.budgets.presentation.BudgetsRoute
import kotlinx.serialization.Serializable

/** Type-safe route for the Budgets destination. */
@Serializable
data object BudgetsRouteKey

fun NavController.navigateToBudgets(navOptions: NavOptions? = null) =
    navigate(BudgetsRouteKey, navOptions)

fun NavGraphBuilder.budgetsScreen() {
    composable<BudgetsRouteKey> {
        BudgetsRoute()
    }
}
