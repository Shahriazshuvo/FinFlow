package com.finflow.feature.dashboard.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.component.EmptyState
import com.finflow.core.designsystem.theme.FinFlowTheme

@Composable
internal fun DashboardScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        EmptyState(
            title = "Dashboard",
            description = "This feature is not built yet.",
        )
    }
}

@Preview
@Composable
private fun DashboardScreenPreview() {
    FinFlowTheme {
        DashboardScreen()
    }
}
