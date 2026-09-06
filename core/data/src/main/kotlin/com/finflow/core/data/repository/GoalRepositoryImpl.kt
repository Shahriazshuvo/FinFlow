package com.finflow.core.data.repository

import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.common.result.getOrNull
import com.finflow.core.common.result.runCatchingApp
import com.finflow.core.data.session.CurrentUserProvider
import com.finflow.core.data.sync.Syncable
import com.finflow.core.data.sync.Synchronizer
import com.finflow.core.data.sync.SyncTrigger
import com.finflow.core.data.sync.TableSyncRunner
import com.finflow.core.database.datasource.GoalLocalDataSource
import com.finflow.core.domain.repository.GoalRepository
import com.finflow.core.model.Goal
import com.finflow.core.model.GoalDraft
import com.finflow.core.model.Money
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.SyncTable
import com.finflow.core.network.datasource.GoalRemoteDataSource
import com.finflow.core.network.error.NetworkErrorMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GoalRepositoryImpl @Inject constructor(
    private val local: GoalLocalDataSource,
    private val remote: GoalRemoteDataSource,
    private val currentUser: CurrentUserProvider,
    private val errorMapper: NetworkErrorMapper,
    private val syncTrigger: SyncTrigger,
    private val clock: Clock,
) : GoalRepository, Syncable {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeGoals(): Flow<List<Goal>> =
        currentUser.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else local.observe(userId)
        }

    override fun observeGoal(id: String): Flow<Goal?> = local.observeById(id)

    override suspend fun save(draft: GoalDraft): AppResult<String> {
        val userId = currentUser.requireUserId().getOrNull()
            ?: return AppResult.Failure(AppError.Unauthorized)
        val now = clock.instant()
        val existing = draft.id?.let { local.getById(it) }

        val goal = Goal(
            id = existing?.id ?: draft.id ?: UUID.randomUUID().toString(),
            userId = userId,
            name = draft.name.trim(),
            targetAmount = draft.targetAmount,
            currentAmount = draft.currentAmount,
            targetDate = draft.targetDate,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            syncStatus = when (existing?.syncStatus) {
                null, SyncStatus.PENDING_CREATE -> SyncStatus.PENDING_CREATE
                else -> SyncStatus.PENDING_UPDATE
            },
        )

        local.upsert(goal, now)
        syncTrigger.requestSync()
        return AppResult.Success(goal.id)
    }

    override suspend fun addContribution(id: String, amount: Money): AppResult<Unit> {
        val goal = local.getById(id)
            ?: return AppResult.Failure(AppError.Unknown("Goal not found"))
        val now = clock.instant()
        local.upsert(
            goal.copy(
                currentAmount = goal.currentAmount + amount,
                updatedAt = now,
                syncStatus = if (goal.syncStatus == SyncStatus.PENDING_CREATE) {
                    SyncStatus.PENDING_CREATE
                } else {
                    SyncStatus.PENDING_UPDATE
                },
            ),
            now,
        )
        syncTrigger.requestSync()
        return AppResult.Success(Unit)
    }

    override suspend fun delete(id: String): AppResult<Unit> {
        val existing = local.getById(id)
            ?: return AppResult.Failure(AppError.Unknown("Goal not found"))
        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            local.deleteHard(id)
        } else {
            local.markDeleted(id, clock.instant())
            syncTrigger.requestSync()
        }
        return AppResult.Success(Unit)
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        val userId = currentUser.userIdOrNull() ?: return false
        val result = runCatchingApp(errorMapper::map) {
            TableSyncRunner(
                table = SyncTable.GOALS,
                clock = clock,
                idOf = Goal::id,
                updatedAtOf = Goal::updatedAt,
                getPending = local::getPending,
                pushUpsert = remote::upsert,
                markSynced = local::markSynced,
                deleteLocal = local::deleteHard,
                fetchSince = remote::fetchSince,
                applyRemote = local::upsertFromRemote,
            ).run(userId, synchronizer)
        }
        return result is AppResult.Success
    }
}
