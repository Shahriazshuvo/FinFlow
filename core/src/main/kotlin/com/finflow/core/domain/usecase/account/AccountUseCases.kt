package com.finflow.core.domain.usecase.account

import com.finflow.core.common.result.AppResult
import com.finflow.core.domain.repository.AccountRepository
import com.finflow.core.model.Account
import com.finflow.core.model.AccountWithBalance
import com.finflow.core.model.AccountDraft
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAccountsUseCase @Inject constructor(
    private val repository: AccountRepository,
) {
    operator fun invoke(): Flow<List<Account>> = repository.observeAccounts()
    //todo: uiStateAcccount
}

class ObserveAccountBalancesUseCase @Inject constructor(
    private val repository: AccountRepository,
) {
    operator fun invoke(): Flow<List<AccountWithBalance>> = repository.observeAccountBalances()
}

class SaveAccountUseCase @Inject constructor(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(draft: AccountDraft): AppResult<String> = repository.save(draft)
}

class DeleteAccountUseCase @Inject constructor(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(id: String): AppResult<Unit> = repository.delete(id)
}
