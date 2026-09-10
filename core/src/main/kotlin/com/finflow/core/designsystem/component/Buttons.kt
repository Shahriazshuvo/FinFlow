package com.finflow.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.theme.FinFlowTheme

/**
 * The app's primary action button. It owns its busy state, so no screen has to hand-roll
 * "button plus spinner" or forget to disable the button while a request is in flight.
 */
@Composable
fun FinFlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
    ) {
        ButtonContent(text = text, isLoading = isLoading, leadingIcon = leadingIcon)
    }
}

@Composable
fun FinFlowOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
    ) {
        ButtonContent(text = text, isLoading = isLoading, leadingIcon = leadingIcon)
    }
}

@Composable
fun FinFlowTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(text = text)
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    leadingIcon: ImageVector?,
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(FinFlowTheme.dimens.progressIndicatorSm),
            strokeWidth = FinFlowTheme.dimens.strokeMedium,
        )
        return
    }
    if (leadingIcon != null) {
        Icon(imageVector = leadingIcon, contentDescription = null)
        Spacer(Modifier.width(FinFlowTheme.spacing.sm))
    }
    Text(text = text)
}

@Preview
@Composable
private fun FinFlowButtonPreview() {
    FinFlowTheme {
        Row {
            FinFlowButton(text = "Save", onClick = {})
            Spacer(Modifier.width(FinFlowTheme.spacing.sm))
            FinFlowOutlinedButton(text = "Cancel", onClick = {})
        }
    }
}