package com.finflow.feature.accounts.presentation.contract

import com.finflow.core.model.AccountType
import com.finflow.core.ui.mvi.UiIntent

/**
 * Everything the accounts screen can send to
 * [com.finflow.feature.accounts.presentation.viewmodel.AccountsViewModel].
 */
sealed interface AccountsIntent : UiIntent {
    data object AddClicked : AccountsIntent

    data class EditClicked(val id: String) : AccountsIntent

    data class NameChanged(val value: String) : AccountsIntent

    data class TypeChanged(val value: AccountType) : AccountsIntent

    data class OpeningBalanceChanged(val value: String) : AccountsIntent

    data object EditorDismissed : AccountsIntent

    data object EditorSubmitted : AccountsIntent

    data class DeleteClicked(val id: String) : AccountsIntent

    data object DeleteConfirmed : AccountsIntent

    data object DeleteDismissed : AccountsIntent

    data object ErrorDismissed : AccountsIntent
}