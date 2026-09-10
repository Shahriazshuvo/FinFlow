package com.finflow.feature.accounts.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.component.AmountCard
import com.finflow.core.designsystem.component.ConfirmDialog
import com.finflow.core.designsystem.component.EmptyState
import com.finflow.core.designsystem.component.FinFlowCard
import com.finflow.core.designsystem.component.FinFlowButton
import com.finflow.core.designsystem.component.FinFlowTextField
import com.finflow.core.designsystem.component.FinFlowTopAppBar
import com.finflow.core.designsystem.component.LoadingState
import com.finflow.core.designsystem.component.SyncIndicator
import com.finflow.core.designsystem.component.SyncStatusChip
import com.finflow.core.designsystem.icon.FinFlowIcons
import com.finflow.core.designsystem.theme.FinFlowTheme
import com.finflow.core.model.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountsScreen(
    state: AccountsState,
    onIntent: (AccountsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { FinFlowTopAppBar(title = "Accounts") },
        floatingActionButton = {
            FloatingActionButton(onClick = { onIntent(AccountsIntent.AddClicked) }) {
                Icon(FinFlowIcons.Add, contentDescription = "Add account")
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(innerPadding))

            state.isEmpty -> EmptyState(
                title = "No accounts yet",
                description = "Add a cash, bank, card or wallet account to start tracking.",
                modifier = Modifier.padding(innerPadding),
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = FinFlowTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.md),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    vertical = FinFlowTheme.spacing.lg,
                ),
            ) {
                item {
                    AmountCard(title = "Total balance", amount = state.totalBalance)
                }
                items(state.accounts, key = { it.id }) { account ->
                    AccountRow(
                        account = account,
                        onEdit = { onIntent(AccountsIntent.EditClicked(account.id)) },
                        onDelete = { onIntent(AccountsIntent.DeleteClicked(account.id)) },
                    )
                }
            }
        }
    }

    if (state.editor != null) {
        ModalBottomSheet(onDismissRequest = { onIntent(AccountsIntent.EditorDismissed) }) {
            AccountEditor(editor = state.editor, onIntent = onIntent)
        }
    }

    if (state.pendingDeleteId != null) {
        ConfirmDialog(
            title = "Delete account?",
            message = "The account is hidden everywhere, and its transactions are kept.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = { onIntent(AccountsIntent.DeleteConfirmed) },
            onDismiss = { onIntent(AccountsIntent.DeleteDismissed) },
        )
    }
}

@Composable
private fun AccountRow(
    account: AccountUiModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FinFlowCard(modifier = modifier, onClick = onEdit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = account.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = account.typeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = account.balance, style = MaterialTheme.typography.titleMedium)
                if (account.isPendingSync) {
                    SyncStatusChip(SyncIndicator.PENDING)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(FinFlowIcons.Delete, contentDescription = "Delete ${account.name}")
            }
        }
    }
}

@Composable
private fun AccountEditor(
    editor: AccountsState.Editor,
    onIntent: (AccountsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(FinFlowTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.md),
    ) {
        Text(
            text = if (editor.isEditing) "Edit account" else "New account",
            style = MaterialTheme.typography.titleLarge,
        )

        FinFlowTextField(
            value = editor.name,
            onValueChange = { onIntent(AccountsIntent.NameChanged(it)) },
            label = "Name",
            errorMessage = editor.nameError,
            enabled = !editor.isSaving,
        )

        AccountTypePicker(
            selected = editor.type,
            enabled = !editor.isSaving,
            onSelected = { onIntent(AccountsIntent.TypeChanged(it)) },
        )

        FinFlowTextField(
            value = editor.openingBalance,
            onValueChange = { onIntent(AccountsIntent.OpeningBalanceChanged(it)) },
            label = "Opening balance",
            placeholder = "0.00",
            errorMessage = editor.openingBalanceError,
            enabled = !editor.isSaving,
            keyboardType = KeyboardType.Number,
        )

        FinFlowButton(
            text = if (editor.isEditing) "Save" else "Add account",
            isLoading = editor.isSaving,
            enabled = editor.canSubmit,
            onClick = { onIntent(AccountsIntent.EditorSubmitted) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AccountTypePicker(
    selected: AccountType,
    enabled: Boolean,
    onSelected: (AccountType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.sm),
    ) {
        AccountType.entries.forEach { type ->
            androidx.compose.material3.FilterChip(
                selected = type == selected,
                enabled = enabled,
                onClick = { onSelected(type) },
                label = { Text(type.label) },
            )
        }
    }
}

@Preview
@Composable
private fun AccountsScreenPreview() {
    FinFlowTheme {
        AccountsScreen(
            state = AccountsState(
                isLoading = false,
                totalBalance = "$2,480.00",
                accounts = listOf(
                    AccountUiModel(
                        id = "1",
                        name = "Cash",
                        type = AccountType.CASH,
                        typeLabel = "Cash",
                        balance = "$180.00",
                        openingBalanceInput = "0",
                        isPendingSync = false,
                    ),
                    AccountUiModel(
                        id = "2",
                        name = "Salary account",
                        type = AccountType.BANK,
                        typeLabel = "Bank",
                        balance = "$2,300.00",
                        openingBalanceInput = "2000",
                        isPendingSync = true,
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}