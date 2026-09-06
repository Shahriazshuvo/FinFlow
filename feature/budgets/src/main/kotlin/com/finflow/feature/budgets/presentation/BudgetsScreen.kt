package com.finflow.feature.budgets.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.component.EmptyState
import com.finflow.core.designsystem.theme.FinFlowTheme

@Composable
internal fun BudgetsScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        EmptyState(
            title = "Budgets",
            description = "This feature is not built yet.",
        )
    }
}

@Preview
@Composable
private fun BudgetsScreenPreview() {
    FinFlowTheme {
        BudgetsScreen()
    }
}
