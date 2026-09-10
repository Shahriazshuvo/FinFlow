package com.finflow.feature.accounts.presentation.contract

import com.finflow.core.model.AccountType
import com.finflow.core.ui.mvi.UiState

/**
 * Accounts list plus the add/edit sheet (APP_SPEC.md §13).
 *
 * Balances arrive already formatted: `Money` does not cross into the screen, so the
 * composable has nothing to get wrong about currency or rounding.
 */
data class AccountsState(
    val isLoading: Boolean = true,
    val accounts: List<AccountUiModel> = emptyList(),
    val totalBalance: String = "",
    val editor: Editor? = null,
    val pendingDeleteId: String? = null,
    val errorMessage: String? = null,
) : UiState {

    val isEmpty: Boolean get() = !isLoading && accounts.isEmpty()

    /** Open add/edit form. `null` means closed — visibility is state, the sheet is not. */
    data class Editor(
        val id: String? = null,
        val name: String = "",
        val type: AccountType = AccountType.CASH,
        val openingBalance: String = "",
        val nameError: String? = null,
        val openingBalanceError: String? = null,
        val isSaving: Boolean = false,
    ) {
        val isEditing: Boolean get() = id != null

        val canSubmit: Boolean get() = !isSaving && name.isNotBlank()
    }
}

/**
 * One row. [balance] is derived — opening balance plus income minus expense — and is never
 * stored anywhere, so it cannot disagree with the transaction list.
 */
data class AccountUiModel(
    val id: String,
    val name: String,
    val type: AccountType,
    val typeLabel: String,
    val balance: String,
    /** Plain major-units text, for seeding the edit form rather than for display. */
    val openingBalanceInput: String,
    val isPendingSync: Boolean,
)