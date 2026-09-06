package com.finflow.core.domain.repository

import com.finflow.core.common.result.AppResult
import com.finflow.core.model.Transaction
import com.finflow.core.model.TransactionDraft
import com.finflow.core.model.TransactionFilter
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    /** Reads come from Room and never wait on the network (APP_SPEC.md §12). */
    fun observeTransactions(filter: TransactionFilter = TransactionFilter.None): Flow<List<Transaction>>

    fun observeRecentTransactions(limit: Int): Flow<List<Transaction>>

    fun observeTransaction(id: String): Flow<Transaction?>

    /** Writes hit Room first and mark the row pending; sync happens in the background. */
    suspend fun save(draft: TransactionDraft): AppResult<String>

    /** Soft delete: marks `deletedAt` and `PENDING_DELETE`. */
    suspend fun delete(id: String): AppResult<Unit>
}
