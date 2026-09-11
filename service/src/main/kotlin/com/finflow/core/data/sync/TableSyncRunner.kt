package com.finflow.core.data.sync

import com.finflow.core.model.PendingRecord
import com.finflow.core.model.RemoteRecord
import com.finflow.core.model.SyncStatus
import java.time.Clock
import java.time.Instant

/**
 * The push-then-pull algorithm from APP_SPEC.md §12, written once and parameterised per
 * table rather than copied into six repositories.
 *
 * Order matters: pushing first means a locally-edited row reaches the server with a fresh
 * `updated_at` before the pull runs, so last-write-wins resolves in favour of the local
 * edit. Anything that still failed to push keeps local precedence and is skipped by the
 * pull, so a network blip can never silently discard a user's offline change.
 */
internal class TableSyncRunner<T : Any>(
    private val table: SyncTable,
    private val clock: Clock,
    private val idOf: (T) -> String,
    private val updatedAtOf: (T) -> Instant,
    private val getPending: suspend () -> List<PendingRecord<T>>,
    private val pushUpsert: suspend (record: T, deletedAt: Instant?) -> Unit,
    private val markSynced: suspend (id: String, syncedAt: Instant, remoteUpdatedAt: Instant?) -> Unit,
    private val deleteLocal: suspend (id: String) -> Unit,
    private val fetchSince: suspend (userId: String, since: Instant?) -> List<RemoteRecord<T>>,
    private val applyRemote: suspend (records: List<T>, now: Instant) -> Unit,
) {

    suspend fun run(userId: String, synchronizer: Synchronizer) {
        push()
        pull(userId, synchronizer)
    }

    private suspend fun push() {
        getPending().forEach { pending ->
            val now = clock.instant()
            when (pending.syncStatus) {
                SyncStatus.PENDING_DELETE -> {
                    pushUpsert(pending.record, pending.deletedAt ?: now)
                    // The tombstone is on the server now; the local row can go.
                    deleteLocal(pending.id)
                }

                SyncStatus.PENDING_CREATE, SyncStatus.PENDING_UPDATE -> {
                    pushUpsert(pending.record, null)
                    markSynced(pending.id, now, updatedAtOf(pending.record))
                }

                SyncStatus.SYNCED -> Unit
            }
        }
    }

    private suspend fun pull(userId: String, synchronizer: Synchronizer) {
        val remote = fetchSince(userId, synchronizer.lastSyncedAt(table))
        if (remote.isEmpty()) return

        val stillPending = getPending().mapTo(mutableSetOf()) { it.id }
        val (tombstones, live) = remote.partition { it.deletedAt != null }

        tombstones.map { idOf(it.record) }
            .filterNot(stillPending::contains)
            .forEach { deleteLocal(it) }

        val toApply = live.map { it.record }.filterNot { idOf(it) in stillPending }
        if (toApply.isNotEmpty()) applyRemote(toApply, clock.instant())

        synchronizer.updateLastSyncedAt(table, remote.maxOf { updatedAtOf(it.record) })
    }
}
