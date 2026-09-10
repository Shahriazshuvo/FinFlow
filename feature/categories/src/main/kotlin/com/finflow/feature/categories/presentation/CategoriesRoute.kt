package com.finflow.feature.categories.presentation

import androidx.compose.runtime.Composable

/**
 * Entry point for the Categories feature. `CategoryUseCases` in `core:domain` is complete;
 * the MVI contract, ViewModel and screen arrive in a later phase.
 */
@Composable
fun CategoriesRoute() {
    CategoriesScreen()
}
