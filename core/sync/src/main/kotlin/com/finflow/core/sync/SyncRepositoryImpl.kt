package com.finflow.core.sync

import com.finflow.core.common.result.AppResult
import com.finflow.core.common.result.runCatchingApp
import com.finflow.core.data.sync.PendingChangesMonitor
import com.finflow.core.datastore.FinFlowPreferencesDataSource
import com.finflow.core.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SyncRepositoryImpl @Inject constructor(
    private val syncManager: SyncManager,
    private val pendingChanges: PendingChangesMonitor,
    private val preferences: FinFlowPreferencesDataSource,
) : SyncRepository {

    override val isSyncing: Flow<Boolean> = syncManager.isSyncing

    override val lastSyncedAt: Flow<Instant?> = preferences.lastSyncedAt

    override val hasPendingChanges: Flow<Boolean> = pendingChanges.hasPendingChanges

    /** Returns as soon as the work is enqueued; the UI never blocks on the network. */
    override suspend fun requestSync(): AppResult<Unit> = runCatchingApp {
        syncManager.requestSync()
    }
}
