package com.finflow.feature.categories.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.finflow.core.navigation.CategoriesRouteKey
import com.finflow.feature.categories.presentation.CategoriesRoute

/** Registers the destination. The route key itself lives in `core:navigation`. */
fun NavGraphBuilder.categoriesScreen() {
    composable<CategoriesRouteKey> {
        CategoriesRoute()
    }
}
