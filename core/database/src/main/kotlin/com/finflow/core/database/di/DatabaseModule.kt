package com.finflow.core.database.di

import android.content.Context
import androidx.room.Room
import com.finflow.core.database.FinFlowDatabase
import com.finflow.core.database.dao.AccountDao
import com.finflow.core.database.dao.BudgetDao
import com.finflow.core.database.dao.CategoryDao
import com.finflow.core.database.dao.GoalDao
import com.finflow.core.database.dao.ProfileDao
import com.finflow.core.database.dao.SyncStateDao
import com.finflow.core.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinFlowDatabase =
        Room.databaseBuilder(context, FinFlowDatabase::class.java, FinFlowDatabase.NAME)
            .build()

    @Provides
    fun provideProfileDao(database: FinFlowDatabase): ProfileDao = database.profileDao()

    @Provides
    fun provideAccountDao(database: FinFlowDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideCategoryDao(database: FinFlowDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: FinFlowDatabase): TransactionDao =
        database.transactionDao()

    @Provides
    fun provideBudgetDao(database: FinFlowDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideGoalDao(database: FinFlowDatabase): GoalDao = database.goalDao()

    @Provides
    fun provideSyncStateDao(database: FinFlowDatabase): SyncStateDao = database.syncStateDao()
}
