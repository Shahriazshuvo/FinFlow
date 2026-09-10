package com.finflow.feature.accounts.presentation

import com.finflow.core.ui.mvi.UiEffect

/** One-shot events from [AccountsViewModel]. Never state — these must not survive rotation. */
sealed interface AccountsEffect : UiEffect {
    data class ShowMessage(val message: String) : AccountsEffect
}