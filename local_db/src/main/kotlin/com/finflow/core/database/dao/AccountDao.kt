package com.finflow.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.finflow.core.database.entity.AccountEntity
import com.finflow.core.database.model.AccountBalanceRow
import com.finflow.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
internal interface AccountDao {

    @Upsert
    suspend fun upsert(account: AccountEntity)

    @Upsert
    suspend fun upsertAll(accounts: List<AccountEntity>)

    @Query(
        """
        SELECT * FROM accounts
        WHERE user_id = :userId AND deleted_at IS NULL
        ORDER BY name COLLATE NOCASE ASC
        """,
    )
    fun observeAll(userId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id AND deleted_at IS NULL")
    fun observeById(id: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: String): AccountEntity?

    /**
     * Balance per account: opening balance plus income minus expense. Computed in SQL so
     * the list stays correct without loading every transaction into memory.
     */
    @Query(
        """
        SELECT a.id AS account_id,
               a.opening_balance_minor + COALESCE((
                   SELECT SUM(CASE WHEN t.type = 'INCOME' THEN t.amount_minor
                                   ELSE -t.amount_minor END)
                   FROM transactions t
                   WHERE t.account_id = a.id AND t.deleted_at IS NULL
               ), 0) AS balance_minor
        FROM accounts a
        WHERE a.user_id = :userId AND a.deleted_at IS NULL
        """,
    )
    fun observeBalances(userId: String): Flow<List<AccountBalanceRow>>

    @Query("SELECT * FROM accounts WHERE sync_status != :synced")
    suspend fun getPending(synced: SyncStatus = SyncStatus.SYNCED): List<AccountEntity>

    @Query(
        """
        UPDATE accounts
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
        UPDATE accounts
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

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteHard(id: String)

    @Query("DELETE FROM accounts")
    suspend fun clear()
}
