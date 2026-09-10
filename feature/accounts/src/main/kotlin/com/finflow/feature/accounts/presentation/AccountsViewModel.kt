package com.finflow.feature.accounts.presentation

import androidx.lifecycle.viewModelScope
import com.finflow.core.common.result.AppResult
import com.finflow.core.domain.usecase.preferences.ObserveCurrencyCodeUseCase
import com.finflow.core.model.AccountDraft
import com.finflow.core.model.Money
import com.finflow.core.ui.error.toUserMessage
import com.finflow.core.ui.mvi.MviViewModel
import com.finflow.core.domain.usecase.account.DeleteAccountUseCase
import com.finflow.core.domain.usecase.account.ObserveAccountBalancesUseCase
import com.finflow.core.domain.usecase.account.SaveAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
internal class AccountsViewModel @Inject constructor(
    observeAccountBalances: ObserveAccountBalancesUseCase,
    observeCurrencyCode: ObserveCurrencyCodeUseCase,
    private val saveAccount: SaveAccountUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val mapper: AccountsUiMapper,
) : MviViewModel<AccountsState, AccountsIntent, AccountsEffect>(AccountsState()) {

    init {
        // Room is the source of truth, so the list is a Flow rather than something a Load
        // intent has to fetch. Sync updates Room and the screen follows on its own.
        combine(
            observeAccountBalances(),
            observeCurrencyCode(),
        ) { balances, currencyCode ->
            setState {
                copy(
                    isLoading = false,
                    accounts = mapper.toUiModels(balances, currencyCode),
                    totalBalance = mapper.totalBalance(balances, currencyCode),
                )
            }
        }.launchIn(viewModelScope)
    }

    override suspend fun handleIntent(intent: AccountsIntent) {
        when (intent) {
            AccountsIntent.AddClicked ->
                setState { copy(editor = AccountsState.Editor()) }

            is AccountsIntent.EditClicked -> openEditor(intent.id)

            is AccountsIntent.NameChanged -> setState {
                copy(editor = editor?.copy(name = intent.value, nameError = null))
            }

            is AccountsIntent.TypeChanged -> setState {
                copy(editor = editor?.copy(type = intent.value))
            }

            is AccountsIntent.OpeningBalanceChanged -> setState {
                copy(
                    editor = editor?.copy(
                        openingBalance = intent.value,
                        openingBalanceError = null,
                    ),
                )
            }

            AccountsIntent.EditorDismissed -> setState { copy(editor = null) }

            AccountsIntent.EditorSubmitted -> submit()

            is AccountsIntent.DeleteClicked -> setState { copy(pendingDeleteId = intent.id) }

            AccountsIntent.DeleteDismissed -> setState { copy(pendingDeleteId = null) }

            AccountsIntent.DeleteConfirmed -> confirmDelete()

            AccountsIntent.ErrorDismissed -> setState { copy(errorMessage = null) }
        }
    }

    private fun openEditor(id: String) {
        val account = currentState.accounts.firstOrNull { it.id == id } ?: return
        setState {
            copy(
                editor = AccountsState.Editor(
                    id = account.id,
                    name = account.name,
                    type = account.type,
                    openingBalance = account.openingBalanceInput,
                ),
            )
        }
    }

    private suspend fun submit() {
        val editor = currentState.editor ?: return
        if (!editor.canSubmit) return

        val openingBalance = parseOpeningBalance(editor.openingBalance)
        if (openingBalance == null) {
            setState {
                copy(editor = editor.copy(openingBalanceError = "Enter a valid amount"))
            }
            return
        }

        setState { copy(editor = editor.copy(isSaving = true)) }

        val result = saveAccount(
            AccountDraft(
                id = editor.id,
                name = editor.name.trim(),
                type = editor.type,
                openingBalance = openingBalance,
            ),
        )

        when (result) {
            is AppResult.Success -> setState { copy(editor = null) }
            is AppResult.Failure -> {
                setState { copy(editor = editor.copy(isSaving = false)) }
                sendEffect(AccountsEffect.ShowMessage(result.error.toUserMessage()))
            }
        }
    }

    private suspend fun confirmDelete() {
        val id = currentState.pendingDeleteId ?: return
        setState { copy(pendingDeleteId = null) }

        // Soft delete. The row is marked PENDING_DELETE locally and the tombstone reaches
        // Supabase on the next sync; transactions that referenced it are untouched.
        val result = deleteAccount(id)
        if (result is AppResult.Failure) {
            sendEffect(AccountsEffect.ShowMessage(result.error.toUserMessage()))
        }
    }

    /**
     * Blank means zero. Unlike a transaction amount this may be negative — a card account
     * can open in debt — so `AmountValidator`, which requires a positive amount, does not
     * apply here.
     */
    private fun parseOpeningBalance(raw: String): Money? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return Money.ZERO
        return try {
            Money.ofMajorUnits(BigDecimal(trimmed))
        } catch (_: NumberFormatException) {
            null
        }
    }
}