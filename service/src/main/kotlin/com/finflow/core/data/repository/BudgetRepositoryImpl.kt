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
import com.finflow.core.database.datasource.BudgetLocalDataSource
import com.finflow.core.domain.repository.BudgetRepository
import com.finflow.core.model.Budget
import com.finflow.core.model.BudgetDraft
import com.finflow.core.model.BudgetUsage
import com.finflow.core.model.SyncStatus
import com.finflow.core.data.sync.SyncTable
import com.finflow.core.network.datasource.BudgetRemoteDataSource
import com.finflow.core.network.error.DataErrorMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Clock
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BudgetRepositoryImpl @Inject constructor(
    private val local: BudgetLocalDataSource,
    private val remote: BudgetRemoteDataSource,
    private val currentUser: CurrentUserProvider,
    private val errorMapper: DataErrorMapper,
    private val syncTrigger: SyncTrigger,
    private val clock: Clock,
) : BudgetRepository, Syncable {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeBudgets(month: YearMonth): Flow<List<Budget>> =
        currentUser.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else local.observeForMonth(userId, month)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeBudgetUsage(month: YearMonth): Flow<List<BudgetUsage>> =
        currentUser.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else local.observeUsageForMonth(userId, month)
        }

    override fun observeBudget(id: String): Flow<Budget?> = local.observeById(id)

    override suspend fun save(draft: BudgetDraft): AppResult<String> {
        val userId = currentUser.requireUserId().getOrNull()
            ?: return AppResult.Failure(AppError.Unauthorized)
        val now = clock.instant()
        val existing = draft.id?.let { local.getById(it) }

        // `budgets_user_category_month_unique_idx` allows one budget per category per
        // month. Room's `@Upsert` would replace the existing row instead of failing, which
        // would look like a successful create and silently discard the earlier amount.
        if (local.hasMonthConflict(userId, draft.categoryId, draft.month, existing?.id)) {
            return AppResult.Failure(AppError.Duplicate(field = "category"))
        }

        val budget = Budget(
            id = existing?.id ?: draft.id ?: UUID.randomUUID().toString(),
            userId = userId,
            categoryId = draft.categoryId,
            month = draft.month,
            amount = draft.amount,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            syncStatus = when (existing?.syncStatus) {
                null, SyncStatus.PENDING_CREATE -> SyncStatus.PENDING_CREATE
                else -> SyncStatus.PENDING_UPDATE
            },
        )

        local.upsert(budget, now)
        syncTrigger.requestSync()
        return AppResult.Success(budget.id)
    }

    override suspend fun delete(id: String): AppResult<Unit> {
        val existing = local.getById(id)
            ?: return AppResult.Failure(AppError.Unknown("Budget not found"))
        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            local.deleteHard(id)
        } else {
            local.markDeleted(id, clock.instant())
            syncTrigger.requestSync()
        }
        return AppResult.Success(Unit)
    }

    /** Pull order is imposed on this, not on Dagger's set iteration. */
    override val table: SyncTable = SyncTable.BUDGETS

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        val userId = currentUser.userIdOrNull() ?: return false
        val result = runCatchingApp(errorMapper::map) {
            TableSyncRunner(
                table = SyncTable.BUDGETS,
                clock = clock,
                idOf = Budget::id,
                updatedAtOf = Budget::updatedAt,
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
