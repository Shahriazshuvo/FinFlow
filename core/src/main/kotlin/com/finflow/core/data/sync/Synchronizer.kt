package com.finflow.core.data.sync

import com.finflow.core.model.SyncTable
import java.time.Instant

/** Owns the per-table incremental-pull watermarks. Backed by DataStore. */
interface Synchronizer {
    suspend fun lastSyncedAt(table: SyncTable): Instant?

    suspend fun updateLastSyncedAt(table: SyncTable, instant: Instant)
}

/**
 * Implemented by every repository that owns a synced table. Returns `true` when the table
 * reached a consistent state, `false` when the worker should retry.
 *
 * [table] is not decoration: implementations are contributed `@IntoSet` from nine different
 * feature modules, and a Dagger set has no defined iteration order. The worker sorts on it
 * so a pulled transaction can never land before the account it points at. [SyncTable]'s
 * declaration order is therefore the single declaration of pull order.
 */
interface Syncable {
    val table: SyncTable

    suspend fun syncWith(synchronizer: Synchronizer): Boolean
}

/**
 * Lets a repository ask for a background sync without depending on WorkManager. The
 * implementation lives in `core:sync`; `core:data` only knows this one method, which keeps
 * the write path free of any scheduling concern.
 */
fun interface SyncTrigger {
    fun requestSync()
}
