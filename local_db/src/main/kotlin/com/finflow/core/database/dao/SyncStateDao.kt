package com.finflow.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SyncStateDao {

    /**
     * How many rows across all tables still owe the server a write. One query rather than
     * six flows, so the sync chip in the UI costs a single observer.
     */
    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM profiles WHERE sync_status != 'SYNCED') +
            (SELECT COUNT(*) FROM accounts WHERE sync_status != 'SYNCED') +
            (SELECT COUNT(*) FROM categories WHERE sync_status != 'SYNCED') +
            (SELECT COUNT(*) FROM transactions WHERE sync_status != 'SYNCED') +
            (SELECT COUNT(*) FROM budgets WHERE sync_status != 'SYNCED') +
            (SELECT COUNT(*) FROM goals WHERE sync_status != 'SYNCED')
        """,
    )
    fun observePendingCount(): Flow<Int>
}
