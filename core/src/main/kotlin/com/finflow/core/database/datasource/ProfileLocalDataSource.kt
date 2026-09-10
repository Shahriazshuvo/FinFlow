package com.finflow.core.database.datasource

import com.finflow.core.database.dao.ProfileDao
import com.finflow.core.database.entity.SyncMetadata
import com.finflow.core.database.mapper.toDomain
import com.finflow.core.database.mapper.toEntity
import com.finflow.core.model.PendingRecord
import com.finflow.core.model.Profile
import com.finflow.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileLocalDataSource @Inject constructor(
    private val dao: ProfileDao,
) {

    fun observeById(id: String): Flow<Profile?> = dao.observeById(id).map { it?.toDomain() }

    suspend fun getById(id: String): Profile? = dao.getById(id)?.toDomain()

    suspend fun upsert(profile: Profile, now: Instant) {
        val previous = dao.getById(profile.id)?.sync
        dao.upsert(profile.toEntity(localUpdatedAt = now, previous = previous))
    }

    suspend fun upsertFromRemote(profile: Profile, now: Instant) {
        dao.upsert(
            profile.copy(syncStatus = SyncStatus.SYNCED).toEntity(
                localUpdatedAt = now,
                previous = SyncMetadata(
                    syncStatus = SyncStatus.SYNCED,
                    localUpdatedAt = now,
                    remoteUpdatedAt = profile.updatedAt,
                    lastSyncedAt = now,
                ),
            ),
        )
    }

    suspend fun getPending(): List<PendingRecord<Profile>> = dao.getPending().map { entity ->
        PendingRecord(
            id = entity.id,
            record = entity.toDomain(),
            syncStatus = entity.sync.syncStatus,
            deletedAt = entity.sync.deletedAt,
        )
    }

    suspend fun markSynced(id: String, syncedAt: Instant, remoteUpdatedAt: Instant?) =
        dao.markSynced(id = id, syncedAt = syncedAt, remoteUpdatedAt = remoteUpdatedAt)

    suspend fun clear() = dao.clear()
}
