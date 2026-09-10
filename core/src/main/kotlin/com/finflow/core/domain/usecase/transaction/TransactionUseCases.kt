package com.finflow.core.domain.usecase.transaction

import com.finflow.core.common.result.AppResult
import com.finflow.core.domain.repository.TransactionRepository
import com.finflow.core.model.Transaction
import com.finflow.core.model.TransactionDraft
import com.finflow.core.model.TransactionFilter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(filter: TransactionFilter = TransactionFilter.None): Flow<List<Transaction>> =
        repository.observeTransactions(filter)
}

class ObserveRecentTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(limit: Int = DEFAULT_LIMIT): Flow<List<Transaction>> =
        repository.observeRecentTransactions(limit)

    private companion object {
        const val DEFAULT_LIMIT = 5
    }
}

class ObserveTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(id: String): Flow<Transaction?> = repository.observeTransaction(id)
}

class SaveTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(draft: TransactionDraft): AppResult<String> =
        repository.save(draft)
}

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(id: String): AppResult<Unit> = repository.delete(id)
}
