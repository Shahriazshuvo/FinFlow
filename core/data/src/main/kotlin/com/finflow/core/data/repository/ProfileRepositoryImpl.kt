package com.finflow.core.data.repository

import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.common.result.getOrNull
import com.finflow.core.common.result.runCatchingApp
import com.finflow.core.data.session.CurrentUserProvider
import com.finflow.core.data.sync.Syncable
import com.finflow.core.data.sync.Synchronizer
import com.finflow.core.database.datasource.ProfileLocalDataSource
import com.finflow.core.datastore.FinFlowPreferencesDataSource
import com.finflow.core.domain.repository.ProfileRepository
import com.finflow.core.model.Profile
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.SyncTable
import com.finflow.core.network.datasource.ProfileRemoteDataSource
import com.finflow.core.network.error.NetworkErrorMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ProfileRepositoryImpl @Inject constructor(
    private val local: ProfileLocalDataSource,
    private val remote: ProfileRemoteDataSource,
    private val preferences: FinFlowPreferencesDataSource,
    private val currentUser: CurrentUserProvider,
    private val errorMapper: NetworkErrorMapper,
    private val clock: Clock,
) : ProfileRepository, Syncable {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeProfile(): Flow<Profile?> =
        currentUser.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(null) else local.observeById(userId)
        }

    override suspend fun updateFullName(fullName: String): AppResult<Unit> =
        update { it.copy(fullName = fullName.trim()) }

    override suspend fun updateCurrencyCode(currencyCode: String): AppResult<Unit> {
        val result = update { it.copy(currencyCode = currencyCode) }
        if (result is AppResult.Success) preferences.setCurrencyCode(currencyCode)
        return result
    }

    private suspend fun update(transform: (Profile) -> Profile): AppResult<Unit> {
        val userId = currentUser.requireUserId().getOrNull()
            ?: return AppResult.Failure(AppError.Unauthorized)
        val existing = local.getById(userId)
            ?: return AppResult.Failure(AppError.Unknown("Profile not available yet"))
        val now = clock.instant()
        local.upsert(
            transform(existing).copy(updatedAt = now, syncStatus = SyncStatus.PENDING_UPDATE),
            now,
        )
        return AppResult.Success(Unit)
    }

    /**
     * Profiles are a single row that the server creates and never deletes, so this does not
     * use [com.finflow.core.data.sync.TableSyncRunner]: push any local edit, then take the
     * server's copy.
     */
    /** Pull order is imposed on this, not on Dagger's set iteration. */
    override val table: SyncTable = SyncTable.PROFILES

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        val userId = currentUser.userIdOrNull() ?: return false
        val result = runCatchingApp(errorMapper::map) {
            local.getPending().forEach { pending ->
                remote.update(pending.record)
                local.markSynced(pending.id, clock.instant(), pending.record.updatedAt)
            }
            remote.fetch(userId)?.let { profile ->
                local.upsertFromRemote(profile, clock.instant())
                synchronizer.updateLastSyncedAt(SyncTable.PROFILES, profile.updatedAt)
            }
        }
        return result is AppResult.Success
    }
}
