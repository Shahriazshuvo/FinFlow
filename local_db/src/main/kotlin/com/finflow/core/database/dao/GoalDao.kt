package com.finflow.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.finflow.core.database.entity.GoalEntity
import com.finflow.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
internal interface GoalDao {

    @Upsert
    suspend fun upsert(goal: GoalEntity)

    @Upsert
    suspend fun upsertAll(goals: List<GoalEntity>)

    @Query(
        """
        SELECT * FROM goals
        WHERE user_id = :userId AND deleted_at IS NULL
        ORDER BY target_date IS NULL, target_date ASC, created_at DESC
        """,
    )
    fun observeAll(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id AND deleted_at IS NULL")
    fun observeById(id: String): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: String): GoalEntity?

    @Query("SELECT * FROM goals WHERE sync_status != :synced")
    suspend fun getPending(synced: SyncStatus = SyncStatus.SYNCED): List<GoalEntity>

    @Query(
        """
        UPDATE goals
        SET sync_status = :status, deleted_at = :deletedAt, local_updated_at = :now
        WHERE id = :id
        """,
    )
    suspend fun markDeleted(
        id: String,
        status: SyncStatus = SyncStatus.PENDING_DELETE,
        deletedAt: Instant,
        now: Instant,
    )

    @Query(
        """
        UPDATE goals
        SET sync_status = :status, last_synced_at = :syncedAt, remote_updated_at = :remoteUpdatedAt
        WHERE id = :id
        """,
    )
    suspend fun markSynced(
        id: String,
        syncedAt: Instant,
        remoteUpdatedAt: Instant?,
        status: SyncStatus = SyncStatus.SYNCED,
    )

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteHard(id: String)

    @Query("DELETE FROM goals")
    suspend fun clear()
}
