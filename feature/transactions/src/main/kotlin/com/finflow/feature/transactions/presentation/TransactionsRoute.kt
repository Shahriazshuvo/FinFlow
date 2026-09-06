package com.finflow.feature.transactions.presentation

import androidx.compose.runtime.Composable

/**
 * Entry point for the Transactions feature. Phase 1/2 ships the route and its place in the
 * navigation graph; the MVI contract, ViewModel and screen arrive in a later phase.
 */
@Composable
fun TransactionsRoute() {
    TransactionsScreen()
}
