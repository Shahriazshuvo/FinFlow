package com.finflow.core.sync

import com.finflow.core.data.sync.Synchronizer
import com.finflow.core.datastore.FinFlowPreferencesDataSource
import com.finflow.core.model.SyncTable
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Watermarks live in DataStore rather than Room: they are device state, and wiping the
 * database on sign-out must not leave the app convinced it is already up to date.
 */
@Singleton
internal class DataStoreSynchronizer @Inject constructor(
    private val preferences: FinFlowPreferencesDataSource,
) : Synchronizer {

    override suspend fun lastSyncedAt(table: SyncTable): Instant? =
        preferences.lastSyncedAt(table).first()

    override suspend fun updateLastSyncedAt(table: SyncTable, instant: Instant) {
        preferences.setLastSyncedAt(table, instant)
    }
}
