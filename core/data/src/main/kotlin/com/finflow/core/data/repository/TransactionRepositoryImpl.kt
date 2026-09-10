package com.finflow.core.data.repository

import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.common.result.getOrNull
import com.finflow.core.common.result.runCatchingApp
import com.finflow.core.data.session.CurrentUserProvider
import com.finflow.core.data.sync.Syncable
import com.finflow.core.data.sync.Synchronizer
import com.finflow.core.data.sync.SyncTrigger
import com.finflow.core.data.sync.TableSyncRunner
import com.finflow.core.database.datasource.TransactionLocalDataSource
import com.finflow.core.domain.repository.TransactionRepository
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.SyncTable
import com.finflow.core.model.Transaction
import com.finflow.core.model.TransactionDraft
import com.finflow.core.model.TransactionFilter
import com.finflow.core.network.datasource.TransactionRemoteDataSource
import com.finflow.core.network.error.DataErrorMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TransactionRepositoryImpl @Inject constructor(
    private val local: TransactionLocalDataSource,
    private val remote: TransactionRemoteDataSource,
    private val currentUser: CurrentUserProvider,
    private val errorMapper: DataErrorMapper,
    private val syncTrigger: SyncTrigger,
    private val clock: Clock,
) : TransactionRepository, Syncable {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>> =
        currentUser.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else local.observe(userId, filter)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRecentTransactions(limit: Int): Flow<List<Transaction>> =
        currentUser.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else local.observeRecent(userId, limit)
        }

    override fun observeTransaction(id: String): Flow<Transaction?> = local.observeById(id)

    /**
     * Room first, always. The row is written with a pending status and the UI updates from
     * the Flow immediately; reaching Supabase is the background worker's problem.
     */
    override suspend fun save(draft: TransactionDraft): AppResult<String> {
        val userId = currentUser.requireUserId().getOrNull()
            ?: return AppResult.Failure(AppError.Unauthorized)
        val now = clock.instant()
        val existing = draft.id?.let { local.getById(it) }

        val transaction = Transaction(
            id = existing?.id ?: draft.id ?: UUID.randomUUID().toString(),
            userId = userId,
            accountId = draft.accountId,
            categoryId = draft.categoryId,
            type = draft.type,
            amount = draft.amount,
            note = draft.note?.trim()?.takeIf(String::isNotEmpty),
            transactionDate = draft.transactionDate,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            // A row that has never reached the server stays PENDING_CREATE even when
            // edited, so sync keeps treating it as an insert.
            syncStatus = when (existing?.syncStatus) {
                null -> SyncStatus.PENDING_CREATE
                SyncStatus.PENDING_CREATE -> SyncStatus.PENDING_CREATE
                else -> SyncStatus.PENDING_UPDATE
            },
        )

        local.upsert(transaction, now)
        syncTrigger.requestSync()
        return AppResult.Success(transaction.id)
    }

    override suspend fun delete(id: String): AppResult<Unit> {
        val existing = local.getById(id)
            ?: return AppResult.Failure(AppError.Unknown("Transaction not found"))
        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            // Never reached the server, so there is no tombstone to publish.
            local.deleteHard(id)
        } else {
            local.markDeleted(id, clock.instant())
            syncTrigger.requestSync()
        }
        return AppResult.Success(Unit)
    }

    /** Pull order is imposed on this, not on Dagger's set iteration. */
    override val table: SyncTable = SyncTable.TRANSACTIONS

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        val userId = currentUser.userIdOrNull() ?: return false
        val result = runCatchingApp(errorMapper::map) {
            runner().run(userId, synchronizer)
        }
        return result is AppResult.Success
    }

    private fun runner() = TableSyncRunner(
        table = SyncTable.TRANSACTIONS,
        clock = clock,
        idOf = Transaction::id,
        updatedAtOf = Transaction::updatedAt,
        getPending = local::getPending,
        pushUpsert = remote::upsert,
        markSynced = local::markSynced,
        deleteLocal = local::deleteHard,
        fetchSince = remote::fetchSince,
        applyRemote = local::upsertFromRemote,
    )
}
