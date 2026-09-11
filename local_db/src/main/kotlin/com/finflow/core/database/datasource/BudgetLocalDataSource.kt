package com.finflow.core.database.datasource

import com.finflow.core.database.dao.BudgetDao
import com.finflow.core.database.entity.SyncMetadata
import com.finflow.core.database.mapper.toDomain
import com.finflow.core.database.mapper.toEntity
import com.finflow.core.model.Budget
import com.finflow.core.model.BudgetUsage
import com.finflow.core.model.Money
import com.finflow.core.model.PendingRecord
import com.finflow.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetLocalDataSource @Inject internal constructor(
    private val dao: BudgetDao,
) {

    fun observeForMonth(userId: String, month: YearMonth): Flow<List<Budget>> =
        dao.observeForMonth(userId, month).map { entities -> entities.map { it.toDomain() } }

    /**
     * Budgets joined with the expense total for the same month. The month boundary is
     * computed here rather than in SQL so it uses the same calendar rules as the UI.
     */
    fun observeUsageForMonth(userId: String, month: YearMonth): Flow<List<BudgetUsage>> = combine(
        dao.observeForMonth(userId, month),
        dao.observeUsageForMonth(
            userId = userId,
            month = month,
            from = month.atDay(1),
            to = month.atEndOfMonth(),
        ),
    ) { budgets, usage ->
        val usageById = usage.associateBy { it.budgetId }
        budgets.mapNotNull { entity ->
            val row = usageById[entity.id] ?: return@mapNotNull null
            BudgetUsage(
                budget = entity.toDomain(),
                categoryName = row.categoryName,
                categoryColor = row.categoryColor,
                spent = Money(row.spentMinor),
            )
        }
    }

    fun observeById(id: String): Flow<Budget?> = dao.observeById(id).map { it?.toDomain() }

    suspend fun getById(id: String): Budget? = dao.getById(id)?.toDomain()

    /** True when this category already has a budget for [month]. */
    suspend fun hasMonthConflict(
        userId: String,
        categoryId: String,
        month: YearMonth,
        excludingId: String?,
    ): Boolean = dao.findConflictingId(userId, categoryId, month, excludingId) != null

    suspend fun upsert(budget: Budget, now: Instant) {
        val previous = dao.getById(budget.id)?.sync
        dao.upsert(budget.toEntity(localUpdatedAt = now, previous = previous))
    }

    /** Remote write: the row already matches the server, so it lands as SYNCED. */
    suspend fun upsertFromRemote(budgets: List<Budget>, now: Instant) {
        dao.upsertAll(
            budgets.map { record ->
                record.copy(syncStatus = SyncStatus.SYNCED).toEntity(
                    localUpdatedAt = now,
                    previous = SyncMetadata(
                        syncStatus = SyncStatus.SYNCED,
                        localUpdatedAt = now,
                        remoteUpdatedAt = record.updatedAt,
                        lastSyncedAt = now,
                        deletedAt = null,
                    ),
                )
            },
        )
    }

    suspend fun markDeleted(id: String, now: Instant) =
        dao.markDeleted(id = id, deletedAt = now, now = now)

    suspend fun getPending(): List<PendingRecord<Budget>> = dao.getPending().map { entity ->
        PendingRecord(
            id = entity.id,
            record = entity.toDomain(),
            syncStatus = entity.sync.syncStatus,
            deletedAt = entity.sync.deletedAt,
        )
    }

    suspend fun markSynced(id: String, syncedAt: Instant, remoteUpdatedAt: Instant?) =
        dao.markSynced(id = id, syncedAt = syncedAt, remoteUpdatedAt = remoteUpdatedAt)

    suspend fun deleteHard(id: String) = dao.deleteHard(id)

    suspend fun clear() = dao.clear()
}
