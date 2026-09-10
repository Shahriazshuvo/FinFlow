package com.finflow.core.data.sync

import com.finflow.core.database.datasource.SyncStateLocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Whether anything is still waiting to reach the server.
 *
 * This is an interface rather than a concrete class so `core:sync` can consume it without
 * `core:database` appearing on its classpath — the Room boundary stays inside `core:data`.
 */
interface PendingChangesMonitor {
    val hasPendingChanges: Flow<Boolean>
}

@Singleton
internal class RoomPendingChangesMonitor @Inject constructor(
    syncState: SyncStateLocalDataSource,
) : PendingChangesMonitor {
    override val hasPendingChanges: Flow<Boolean> = syncState.hasPendingChanges
}
