package com.finflow.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.finflow.core.database.entity.BudgetEntity
import com.finflow.core.database.model.BudgetUsageRow
import com.finflow.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

@Dao
interface BudgetDao {

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Upsert
    suspend fun upsertAll(budgets: List<BudgetEntity>)

    @Query(
        """
        SELECT * FROM budgets
        WHERE user_id = :userId AND month = :month AND deleted_at IS NULL
        ORDER BY amount_minor DESC
        """,
    )
    fun observeForMonth(userId: String, month: YearMonth): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id AND deleted_at IS NULL")
    fun observeById(id: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: String): BudgetEntity?

    /**
     * Budget spend for a month. The date range is passed in rather than derived in SQL so
     * the month boundary is computed once, in Kotlin, with the same calendar rules the UI
     * uses.
     */
    @Query(
        """
        SELECT b.id AS budget_id, c.name AS category_name, c.color AS category_color,
               COALESCE((
                   SELECT SUM(t.amount_minor) FROM transactions t
                   WHERE t.category_id = b.category_id
                     AND t.user_id = b.user_id
                     AND t.type = 'EXPENSE'
                     AND t.deleted_at IS NULL
                     AND t.transaction_date BETWEEN :from AND :to
               ), 0) AS spent_minor
        FROM budgets b
        INNER JOIN categories c ON c.id = b.category_id
        WHERE b.user_id = :userId AND b.month = :month AND b.deleted_at IS NULL
        """,
    )
    fun observeUsageForMonth(
        userId: String,
        month: YearMonth,
        from: LocalDate,
        to: LocalDate,
    ): Flow<List<BudgetUsageRow>>

    @Query("SELECT * FROM budgets WHERE sync_status != :synced")
    suspend fun getPending(synced: SyncStatus = SyncStatus.SYNCED): List<BudgetEntity>

    @Query(
        """
        UPDATE budgets
        SET sync_status = :status, deleted_at = :deletedAt, local_updated_at = :now
        WHERE id = :id
        """,
    )
    suspend fun markDeleted(
        id: String,
        status: SyncStatus = SyncStatus.PENDING_DELETE,
        deletedAt: Instant,
        now: Instant,
    )

    @Query(
        """
        UPDATE budgets
        SET sync_status = :status, last_synced_at = :syncedAt, remote_updated_at = :remoteUpdatedAt
        WHERE id = :id
        """,
    )
    suspend fun markSynced(
        id: String,
        syncedAt: Instant,
        remoteUpdatedAt: Instant?,
        status: SyncStatus = SyncStatus.SYNCED,
    )

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteHard(id: String)

    @Query("DELETE FROM budgets")
    suspend fun clear()
}
