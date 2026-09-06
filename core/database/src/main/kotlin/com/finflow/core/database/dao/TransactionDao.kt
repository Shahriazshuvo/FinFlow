package com.finflow.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.finflow.core.database.entity.TransactionEntity
import com.finflow.core.database.model.CategoryTotalRow
import com.finflow.core.database.model.TypeTotalRow
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

@Dao
interface TransactionDao {

    @Upsert
    suspend fun upsert(transaction: TransactionEntity)

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions WHERE id = :id AND deleted_at IS NULL")
    fun observeById(id: String): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    /**
     * The single filtered read behind the transactions list. Every filter dimension is
     * optional; the `:x IS NULL OR ...` guards let one query serve all filter combinations
     * without dropping to a RawQuery (which Room cannot verify at compile time).
     */
    @Query(
        """
        SELECT * FROM transactions
        WHERE user_id = :userId
          AND deleted_at IS NULL
          AND (:type IS NULL OR type = :type)
          AND (:ignoreAccounts = 1 OR account_id IN (:accountIds))
          AND (:ignoreCategories = 1 OR category_id IN (:categoryIds))
          AND (:from IS NULL OR transaction_date >= :from)
          AND (:to IS NULL OR transaction_date <= :to)
          AND (:query IS NULL OR note LIKE '%' || :query || '%')
        ORDER BY transaction_date DESC, created_at DESC
        """,
    )
    fun observeFiltered(
        userId: String,
        type: TransactionType?,
        ignoreAccounts: Boolean,
        accountIds: List<String>,
        ignoreCategories: Boolean,
        categoryIds: List<String>,
        from: LocalDate?,
        to: LocalDate?,
        query: String?,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE user_id = :userId AND deleted_at IS NULL
        ORDER BY transaction_date DESC, created_at DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(userId: String, limit: Int): Flow<List<TransactionEntity>>

    // --- Aggregates (APP_SPEC.md §18: analytics is computed from Room) ---

    @Query(
        """
        SELECT type AS type, SUM(amount_minor) AS total_minor
        FROM transactions
        WHERE user_id = :userId AND deleted_at IS NULL
          AND transaction_date BETWEEN :from AND :to
        GROUP BY type
        """,
    )
    fun observeTotalsByType(
        userId: String,
        from: LocalDate,
        to: LocalDate,
    ): Flow<List<TypeTotalRow>>

    @Query(
        """
        SELECT c.id AS category_id, c.name AS category_name, c.color AS category_color,
               SUM(t.amount_minor) AS total_minor
        FROM transactions t
        INNER JOIN categories c ON c.id = t.category_id
        WHERE t.user_id = :userId AND t.deleted_at IS NULL AND c.deleted_at IS NULL
          AND t.type = :type
          AND t.transaction_date BETWEEN :from AND :to
        GROUP BY c.id, c.name, c.color
        ORDER BY total_minor DESC
        """,
    )
    fun observeTotalsByCategory(
        userId: String,
        type: TransactionType,
        from: LocalDate,
        to: LocalDate,
    ): Flow<List<CategoryTotalRow>>

    // --- Sync bookkeeping ---

    @Query("SELECT * FROM transactions WHERE sync_status != :synced")
    suspend fun getPending(synced: SyncStatus = SyncStatus.SYNCED): List<TransactionEntity>

    @Query(
        """
        UPDATE transactions
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
        UPDATE transactions
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

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteHard(id: String)

    @Query("DELETE FROM transactions")
    suspend fun clear()
}
