package com.finflow.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.icon.FinFlowIcons
import com.finflow.core.designsystem.theme.FinFlowTheme

/** Base surface for every card in the app, so radius and elevation stay consistent. */
@Composable
fun FinFlowCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(FinFlowTheme.dimens.cornerLg)
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    )
    val elevation = CardDefaults.cardElevation(
        defaultElevation = FinFlowTheme.dimens.elevationNone,
    )

    if (onClick == null) {
        Card(modifier = modifier, shape = shape, colors = colors, elevation = elevation) {
            content()
        }
    } else {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
        ) {
            content()
        }
    }
}

/**
 * Headline figure card used across dashboard and analytics: a label, a formatted amount,
 * and an optional trend line. [isPositive] drives the semantic income/expense colour.
 */
@Composable
fun AmountCard(
    title: String,
    amount: String,
    modifier: Modifier = Modifier,
    trendLabel: String? = null,
    isPositive: Boolean? = null,
) {
    FinFlowCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FinFlowTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineSmall,
                color = when (isPositive) {
                    true -> FinFlowTheme.colors.income
                    false -> FinFlowTheme.colors.expense
                    null -> MaterialTheme.colorScheme.onSurface
                },
            )
            if (trendLabel != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPositive != null) {
                        Icon(
                            imageVector = if (isPositive) {
                                FinFlowIcons.Income
                            } else {
                                FinFlowIcons.Expense
                            },
                            contentDescription = null,
                            modifier = Modifier.size(FinFlowTheme.dimens.iconSm),
                            tint = if (isPositive) {
                                FinFlowTheme.colors.income
                            } else {
                                FinFlowTheme.colors.expense
                            },
                        )
                        Spacer(Modifier.width(FinFlowTheme.spacing.xs))
                    }
                    Text(
                        text = trendLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Progress card shared by budgets and goals: title, a value/target caption, a progress
 * bar, and a trailing status line.
 */
@Composable
fun ProgressCard(
    title: String,
    primaryValue: String,
    progress: Float,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    statusText: String? = null,
    indicatorColor: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    FinFlowCard(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FinFlowTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = primaryValue,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FinFlowTheme.dimens.progressHeight),
                color = indicatorColor ?: MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
            )

            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (statusText != null) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = indicatorColor ?: MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Preview
@Composable
private fun AmountCardPreview() {
    FinFlowTheme {
        Column(verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.md)) {
            AmountCard(
                title = "This month expense",
                amount = "$1,240.50",
                trendLabel = "12% more than last month",
                isPositive = false,
            )
            ProgressCard(
                title = "Groceries",
                primaryValue = "$320 / $500",
                progress = 0.64f,
                supportingText = "$180 remaining",
                statusText = "On track",
            )
        }
    }
}
