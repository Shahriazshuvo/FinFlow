package com.finflow.feature.transactions.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.component.EmptyState
import com.finflow.core.designsystem.theme.FinFlowTheme

@Composable
internal fun TransactionsScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        EmptyState(
            title = "Transactions",
            description = "This feature is not built yet.",
        )
    }
}

@Preview
@Composable
private fun TransactionsScreenPreview() {
    FinFlowTheme {
        TransactionsScreen()
    }
}
