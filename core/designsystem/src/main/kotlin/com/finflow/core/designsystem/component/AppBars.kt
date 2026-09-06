package com.finflow.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.icon.FinFlowIcons
import com.finflow.core.designsystem.theme.FinFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinFlowTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        modifier = modifier,
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = FinFlowIcons.Back,
                        contentDescription = "Navigate back",
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

/** One entry in the app's bottom navigation. */
data class FinFlowBottomBarItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun FinFlowBottomBar(
    items: List<FinFlowBottomBarItem>,
    selectedKey: String,
    onItemSelected: (FinFlowBottomBarItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.key == selectedKey,
                onClick = { onItemSelected(item) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) },
            )
        }
    }
}

@Preview
@Composable
private fun FinFlowBottomBarPreview() {
    FinFlowTheme {
        FinFlowBottomBar(
            items = listOf(
                FinFlowBottomBarItem("dashboard", "Home", FinFlowIcons.Dashboard),
                FinFlowBottomBarItem("transactions", "Activity", FinFlowIcons.Transactions),
                FinFlowBottomBarItem("budgets", "Budgets", FinFlowIcons.Budget),
                FinFlowBottomBarItem("settings", "Settings", FinFlowIcons.Settings),
            ),
            selectedKey = "dashboard",
            onItemSelected = {},
        )
    }
}
