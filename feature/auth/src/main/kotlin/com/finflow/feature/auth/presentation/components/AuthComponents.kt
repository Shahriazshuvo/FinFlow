package com.finflow.feature.auth.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.finflow.core.designsystem.icon.FinFlowIcons
import com.finflow.core.designsystem.theme.FinFlowTheme

/**
 * The title block both auth screens open with. The heading is set in the serif display face
 * because that is what the design reserves for a screen's subject — a balance, a goal, a
 * person's name — and an auth screen's subject is the account itself.
 */
@Composable
internal fun AuthHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.sm),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = FinFlowTheme.serif,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * The white field container. A hairline border rather than elevation: the design separates
 * surfaces by outline on an off-white ground, so a shadow here would read as a different
 * component than every card on every other screen.
 */
@Composable
internal fun AuthFormCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FinFlowTheme.dimens.cornerLg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = FinFlowTheme.dimens.elevationNone,
        ),
        border = BorderStroke(
            width = FinFlowTheme.dimens.strokeThin,
            color = MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FinFlowTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.md),
        ) {
            content()
        }
    }
}

/**
 * A failure that belongs to the submission rather than to any one input — wrong credentials,
 * offline, a duplicate email. Per-field problems never come here; they go under their own box,
 * where the user can see which one to fix.
 */
@Composable
internal fun AuthFormError(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = FinFlowTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = FinFlowIcons.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(FinFlowTheme.dimens.iconSm),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

/**
 * The cross-link at the foot of each screen. The prompt stays muted and only the action is
 * tinted, so the tappable half is the one that looks tappable.
 */
@Composable
internal fun AuthFooterPrompt(
    prompt: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onAction, enabled = enabled) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}