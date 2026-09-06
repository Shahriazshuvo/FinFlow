package com.finflow.core.domain.repository

import com.finflow.core.common.result.AppResult
import com.finflow.core.model.Account
import com.finflow.core.model.AccountBalance
import com.finflow.core.model.AccountDraft
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>

    fun observeAccountBalances(): Flow<List<AccountBalance>>

    fun observeAccount(id: String): Flow<Account?>

    /** Returns the id of the created or updated account. */
    suspend fun save(draft: AccountDraft): AppResult<String>

    suspend fun delete(id: String): AppResult<Unit>
}
