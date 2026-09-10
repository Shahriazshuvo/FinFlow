package com.finflow.core.domain.repository

import com.finflow.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/** What the UI is allowed to know and ask about replication. */
interface SyncRepository {
    val isSyncing: Flow<Boolean>

    val lastSyncedAt: Flow<Instant?>

    val hasPendingChanges: Flow<Boolean>

    /** Enqueues a one-off sync; returns as soon as the work is scheduled. */
    suspend fun requestSync(): AppResult<Unit>
}
