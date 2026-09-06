package com.finflow.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.icon.FinFlowIcons
import com.finflow.core.designsystem.theme.FinFlowTheme

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.md),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(FinFlowTheme.dimens.progressIndicatorMd),
            )
            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector = FinFlowIcons.Empty,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    StateScaffold(
        modifier = modifier,
        icon = icon,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
        title = title,
        description = description,
        actionLabel = actionLabel,
        onAction = onAction,
    )
}

@Composable
fun ErrorState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actionLabel: String? = "Retry",
    onAction: (() -> Unit)? = null,
) {
    StateScaffold(
        modifier = modifier,
        icon = FinFlowIcons.Error,
        iconTint = MaterialTheme.colorScheme.error,
        title = title,
        description = description,
        actionLabel = actionLabel,
        onAction = onAction,
    )
}

@Composable
private fun StateScaffold(
    modifier: Modifier,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    description: String?,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(FinFlowTheme.spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.md),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(FinFlowTheme.dimens.emptyStateIconSize),
                tint = iconTint,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (actionLabel != null && onAction != null) {
                FinFlowButton(text = actionLabel, onClick = onAction)
            }
        }
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    FinFlowTheme {
        EmptyState(
            title = "No transactions yet",
            description = "Add your first income or expense to get started.",
            actionLabel = "Add transaction",
            onAction = {},
        )
    }
}
