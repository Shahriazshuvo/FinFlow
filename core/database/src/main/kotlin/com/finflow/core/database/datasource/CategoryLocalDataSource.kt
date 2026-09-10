package com.finflow.core.database.datasource

import com.finflow.core.database.dao.CategoryDao
import com.finflow.core.database.entity.SyncMetadata
import com.finflow.core.database.mapper.toDomain
import com.finflow.core.database.mapper.toEntity
import com.finflow.core.model.Category
import com.finflow.core.model.PendingRecord
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryLocalDataSource @Inject constructor(
    private val dao: CategoryDao,
) {

    fun observe(userId: String, type: TransactionType? = null): Flow<List<Category>> =
        dao.observeAll(userId, type).map { entities -> entities.map { it.toDomain() } }

    fun observeById(id: String): Flow<Category?> = dao.observeById(id).map { it?.toDomain() }

    suspend fun getById(id: String): Category? = dao.getById(id)?.toDomain()

    suspend fun countFor(userId: String): Int = dao.countFor(userId)

    /** True when another live category of the same type already uses [name]. */
    suspend fun hasNameConflict(
        userId: String,
        type: TransactionType,
        name: String,
        excludingId: String?,
    ): Boolean = dao.findConflictingId(userId, type, name, excludingId) != null

    suspend fun upsert(category: Category, now: Instant) {
        val previous = dao.getById(category.id)?.sync
        dao.upsert(category.toEntity(localUpdatedAt = now, previous = previous))
    }

    suspend fun upsertAll(categories: List<Category>, now: Instant) {
        dao.upsertAll(categories.map { it.toEntity(localUpdatedAt = now) })
    }

    /** Remote write: the row already matches the server, so it lands as SYNCED. */
    suspend fun upsertFromRemote(categories: List<Category>, now: Instant) {
        dao.upsertAll(
            categories.map { record ->
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

    suspend fun getPending(): List<PendingRecord<Category>> = dao.getPending().map { entity ->
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
