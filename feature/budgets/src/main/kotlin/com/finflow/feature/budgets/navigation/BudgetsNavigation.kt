package com.finflow.feature.budgets.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.finflow.core.navigation.BudgetsRouteKey
import com.finflow.feature.budgets.presentation.BudgetsRoute

/** Registers the destination. The route key itself lives in `core:navigation`. */
fun NavGraphBuilder.budgetsScreen() {
    composable<BudgetsRouteKey> {
        BudgetsRoute()
    }
}
