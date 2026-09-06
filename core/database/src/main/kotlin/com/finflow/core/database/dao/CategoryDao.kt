package com.finflow.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.finflow.core.database.entity.CategoryEntity
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface CategoryDao {

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query(
        """
        SELECT * FROM categories
        WHERE user_id = :userId AND deleted_at IS NULL
          AND (:type IS NULL OR type = :type)
        ORDER BY name COLLATE NOCASE ASC
        """,
    )
    fun observeAll(userId: String, type: TransactionType?): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id AND deleted_at IS NULL")
    fun observeById(id: String): Flow<CategoryEntity?>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories WHERE user_id = :userId AND deleted_at IS NULL")
    suspend fun countFor(userId: String): Int

    @Query("SELECT * FROM categories WHERE sync_status != :synced")
    suspend fun getPending(synced: SyncStatus = SyncStatus.SYNCED): List<CategoryEntity>

    @Query(
        """
        UPDATE categories
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
        UPDATE categories
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

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteHard(id: String)

    @Query("DELETE FROM categories")
    suspend fun clear()
}
