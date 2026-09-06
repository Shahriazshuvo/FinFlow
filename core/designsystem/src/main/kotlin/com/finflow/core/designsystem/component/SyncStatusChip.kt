package com.finflow.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.icon.FinFlowIcons
import com.finflow.core.designsystem.theme.FinFlowTheme

/**
 * What the UI is allowed to say about replication. Deliberately presentation-only: the
 * design system never imports `SyncStatus` from `core:model`, features map to this.
 */
enum class SyncIndicator {
    SYNCED,
    SYNCING,
    PENDING,
    OFFLINE,
}

@Composable
fun SyncStatusChip(
    indicator: SyncIndicator,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val label = when (indicator) {
        SyncIndicator.SYNCED -> "Synced"
        SyncIndicator.SYNCING -> "Syncing"
        SyncIndicator.PENDING -> "Pending"
        SyncIndicator.OFFLINE -> "Offline"
    }
    val icon = when (indicator) {
        SyncIndicator.SYNCED -> FinFlowIcons.SyncDone
        SyncIndicator.SYNCING, SyncIndicator.PENDING -> FinFlowIcons.SyncPending
        SyncIndicator.OFFLINE -> FinFlowIcons.SyncOffline
    }
    val tint = when (indicator) {
        SyncIndicator.SYNCED -> FinFlowTheme.colors.success
        SyncIndicator.SYNCING -> MaterialTheme.colorScheme.primary
        SyncIndicator.PENDING -> FinFlowTheme.colors.warning
        SyncIndicator.OFFLINE -> FinFlowTheme.colors.neutral
    }

    AssistChip(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(FinFlowTheme.dimens.iconSm),
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Preview
@Composable
private fun SyncStatusChipPreview() {
    FinFlowTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.sm)) {
            SyncStatusChip(SyncIndicator.SYNCED)
            SyncStatusChip(SyncIndicator.PENDING)
            SyncStatusChip(SyncIndicator.OFFLINE)
        }
    }
}
