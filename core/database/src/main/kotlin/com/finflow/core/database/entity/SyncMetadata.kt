package com.finflow.core.database.entity

import androidx.room.ColumnInfo
import com.finflow.core.model.SyncStatus
import java.time.Instant

/**
 * Local-only replication bookkeeping, embedded in every syncable entity (APP_SPEC.md §12).
 * None of these columns exist in Supabase apart from `deletedAt`, which maps to the remote
 * `deleted_at` soft-delete column.
 */
data class SyncMetadata(
    @ColumnInfo(name = "sync_status", defaultValue = "SYNCED")
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    @ColumnInfo(name = "local_updated_at")
    val localUpdatedAt: Instant,
    @ColumnInfo(name = "remote_updated_at")
    val remoteUpdatedAt: Instant? = null,
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Instant? = null,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Instant? = null,
)
