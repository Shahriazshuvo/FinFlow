package com.finflow.core.database.datasource

import com.finflow.core.database.dao.TransactionDao
import com.finflow.core.database.entity.SyncMetadata
import com.finflow.core.database.mapper.toDomain
import com.finflow.core.database.mapper.toEntity
import com.finflow.core.model.PendingRecord
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.Transaction
import com.finflow.core.model.TransactionFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The only way anything outside `core:database` reads or writes transactions. It speaks
 * domain models exclusively — [com.finflow.core.database.entity.TransactionEntity] never
 * crosses this boundary.
 */
@Singleton
class TransactionLocalDataSource @Inject constructor(
    private val dao: TransactionDao,
) {

    fun observe(userId: String, filter: TransactionFilter): Flow<List<Transaction>> =
        dao.observeFiltered(
            userId = userId,
            type = filter.type,
            ignoreAccounts = filter.accountIds.isEmpty(),
            accountIds = filter.accountIds.toList(),
            ignoreCategories = filter.categoryIds.isEmpty(),
            categoryIds = filter.categoryIds.toList(),
            from = filter.from,
            to = filter.to,
            query = filter.query?.takeIf(String::isNotBlank),
        ).map { entities -> entities.map { it.toDomain() } }

    fun observeRecent(userId: String, limit: Int): Flow<List<Transaction>> =
        dao.observeRecent(userId, limit).map { entities -> entities.map { it.toDomain() } }

    fun observeById(id: String): Flow<Transaction?> =
        dao.observeById(id).map { it?.toDomain() }

    suspend fun getById(id: String): Transaction? = dao.getById(id)?.toDomain()

    /** Local write: stamps the pending status so the sync engine picks the row up. */
    suspend fun upsert(transaction: Transaction, now: Instant) {
        val previous = dao.getById(transaction.id)?.sync
        dao.upsert(transaction.toEntity(localUpdatedAt = now, previous = previous))
    }

    /** Remote write: the row already matches the server, so it lands as SYNCED. */
    suspend fun upsertFromRemote(transactions: List<Transaction>, now: Instant) {
        dao.upsertAll(
            transactions.map { record ->
                record.copy(syncStatus = SyncStatus.SYNCED).toEntity(
                    localUpdatedAt = now,
                    previous = SyncMetadata(
                        syncStatus = SyncStatus.SYNCED,
                        localUpdatedAt = now,
                        remoteUpdatedAt = record.updatedAt,
                        lastSyncedAt = now,
                        deletedAt = null,
                    ),
                )
            },
        )
    }

    suspend fun markDeleted(id: String, now: Instant) =
        dao.markDeleted(id = id, deletedAt = now, now = now)

    suspend fun getPending(): List<PendingRecord<Transaction>> =
        dao.getPending().map { entity ->
            PendingRecord(
                id = entity.id,
                record = entity.toDomain(),
                syncStatus = entity.sync.syncStatus,
                deletedAt = entity.sync.deletedAt,
            )
        }

    suspend fun markSynced(id: String, syncedAt: Instant, remoteUpdatedAt: Instant?) =
        dao.markSynced(id = id, syncedAt = syncedAt, remoteUpdatedAt = remoteUpdatedAt)

    suspend fun deleteHard(id: String) = dao.deleteHard(id)

    suspend fun clear() = dao.clear()
}
