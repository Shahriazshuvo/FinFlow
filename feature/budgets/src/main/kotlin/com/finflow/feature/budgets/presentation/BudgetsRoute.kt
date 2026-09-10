package com.finflow.feature.budgets.presentation

import androidx.compose.runtime.Composable
import com.finflow.feature.budgets.presentation.screen.BudgetsScreen

/**
 * Entry point for the Budgets feature. Phase 1/2 ships the route and its place in the
 * navigation graph; the MVI contract, ViewModel and screen arrive in a later phase.
 */
@Composable
fun BudgetsRoute() {
    BudgetsScreen()
}
