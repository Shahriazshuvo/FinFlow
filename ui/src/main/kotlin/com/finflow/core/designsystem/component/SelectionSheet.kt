package com.finflow.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.icon.FinFlowIcons
import com.finflow.core.designsystem.theme.FinFlowTheme

/** One selectable row in a [SelectionSheet]. */
data class SelectionOption(
    val key: String,
    val label: String,
    val supportingText: String? = null,
    val icon: ImageVector? = null,
)

/**
 * Generic single-choice bottom sheet — used for picking an account, a category, a date
 * range preset or a currency, so none of those screens ship their own sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheet(
    title: String,
    options: List<SelectionOption>,
    selectedKey: String?,
    onOptionSelected: (SelectionOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    horizontal = FinFlowTheme.spacing.lg,
                    vertical = FinFlowTheme.spacing.sm,
                ),
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(options, key = SelectionOption::key) { option ->
                    ListItem(
                        headlineContent = { Text(option.label) },
                        supportingContent = option.supportingText?.let { { Text(it) } },
                        leadingContent = option.icon?.let {
                            {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    modifier = Modifier.size(FinFlowTheme.dimens.iconMd),
                                )
                            }
                        },
                        trailingContent = {
                            if (option.key == selectedKey) {
                                Icon(
                                    imageVector = FinFlowIcons.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(FinFlowTheme.dimens.iconMd),
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SelectionOptionPreview() {
    FinFlowTheme {
        Column {
            Text("SelectionSheet renders a modal; see the sheet in-app.")
        }
    }
}
