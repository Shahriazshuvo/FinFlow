package com.finflow.core.domain.usecase.sync

import com.finflow.core.common.result.AppResult
import com.finflow.core.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import javax.inject.Inject

/** What the sync status chip renders. */
data class SyncSnapshot(
    val isSyncing: Boolean,
    val hasPendingChanges: Boolean,
    val lastSyncedAt: Instant?,
)

class ObserveSyncStatusUseCase @Inject constructor(
    private val repository: SyncRepository,
) {
    operator fun invoke(): Flow<SyncSnapshot> = combine(
        repository.isSyncing,
        repository.hasPendingChanges,
        repository.lastSyncedAt,
        ::SyncSnapshot,
    )
}

class RequestSyncUseCase @Inject constructor(
    private val repository: SyncRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = repository.requestSync()
}
