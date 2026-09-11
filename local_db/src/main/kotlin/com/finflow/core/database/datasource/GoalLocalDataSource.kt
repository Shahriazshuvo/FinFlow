package com.finflow.core.database.datasource

import com.finflow.core.database.dao.GoalDao
import com.finflow.core.database.entity.SyncMetadata
import com.finflow.core.database.mapper.toDomain
import com.finflow.core.database.mapper.toEntity
import com.finflow.core.model.Goal
import com.finflow.core.model.PendingRecord
import com.finflow.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalLocalDataSource @Inject internal constructor(
    private val dao: GoalDao,
) {

    fun observe(userId: String): Flow<List<Goal>> =
        dao.observeAll(userId).map { entities -> entities.map { it.toDomain() } }

    fun observeById(id: String): Flow<Goal?> = dao.observeById(id).map { it?.toDomain() }

    suspend fun getById(id: String): Goal? = dao.getById(id)?.toDomain()

    suspend fun upsert(goal: Goal, now: Instant) {
        val previous = dao.getById(goal.id)?.sync
        dao.upsert(goal.toEntity(localUpdatedAt = now, previous = previous))
    }

    /** Remote write: the row already matches the server, so it lands as SYNCED. */
    suspend fun upsertFromRemote(goals: List<Goal>, now: Instant) {
        dao.upsertAll(
            goals.map { record ->
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

    suspend fun getPending(): List<PendingRecord<Goal>> = dao.getPending().map { entity ->
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
