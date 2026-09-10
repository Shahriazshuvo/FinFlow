package com.finflow.feature.accounts.presentation.contract

import com.finflow.core.ui.mvi.UiEffect

/**
 * One-shot events from [com.finflow.feature.accounts.presentation.viewmodel.AccountsViewModel].
 * Never state — these must not survive rotation.
 */
sealed interface AccountsEffect : UiEffect {
    data class ShowMessage(val message: String) : AccountsEffect
}