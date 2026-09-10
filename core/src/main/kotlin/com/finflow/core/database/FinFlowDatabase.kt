package com.finflow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.finflow.core.database.converter.FinFlowTypeConverters
import com.finflow.core.database.dao.AccountDao
import com.finflow.core.database.dao.BudgetDao
import com.finflow.core.database.dao.CategoryDao
import com.finflow.core.database.dao.GoalDao
import com.finflow.core.database.dao.ProfileDao
import com.finflow.core.database.dao.SyncStateDao
import com.finflow.core.database.dao.TransactionDao
import com.finflow.core.database.entity.AccountEntity
import com.finflow.core.database.entity.BudgetEntity
import com.finflow.core.database.entity.CategoryEntity
import com.finflow.core.database.entity.GoalEntity
import com.finflow.core.database.entity.ProfileEntity
import com.finflow.core.database.entity.TransactionEntity

/**
 * The single source of truth for all financial data (APP_SPEC.md §8). Schemas are exported
 * to `core/database/schemas` and committed, so migrations can be diffed in review and
 * tested with `MigrationTestHelper`.
 */
@Database(
    entities = [
        ProfileEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(FinFlowTypeConverters::class)
abstract class FinFlowDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun syncStateDao(): SyncStateDao

    internal companion object {
        const val NAME = "finflow.db"
    }
}
