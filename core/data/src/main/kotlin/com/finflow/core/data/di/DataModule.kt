package com.finflow.core.data.di

import com.finflow.core.data.repository.AccountRepositoryImpl
import com.finflow.core.data.repository.AnalyticsRepositoryImpl
import com.finflow.core.data.repository.AuthRepositoryImpl
import com.finflow.core.data.repository.BudgetRepositoryImpl
import com.finflow.core.data.repository.CategoryRepositoryImpl
import com.finflow.core.data.repository.GoalRepositoryImpl
import com.finflow.core.data.repository.ProfileRepositoryImpl
import com.finflow.core.data.repository.TransactionRepositoryImpl
import com.finflow.core.data.repository.UserPreferencesRepositoryImpl
import com.finflow.core.data.sync.PendingChangesMonitor
import com.finflow.core.data.sync.RoomPendingChangesMonitor
import com.finflow.core.data.sync.Syncable
import com.finflow.core.domain.repository.AccountRepository
import com.finflow.core.domain.repository.AnalyticsRepository
import com.finflow.core.domain.repository.AuthRepository
import com.finflow.core.domain.repository.BudgetRepository
import com.finflow.core.domain.repository.CategoryRepository
import com.finflow.core.domain.repository.GoalRepository
import com.finflow.core.domain.repository.ProfileRepository
import com.finflow.core.domain.repository.TransactionRepository
import com.finflow.core.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

/**
 * Every repository binding in the app (APP_SPEC.md §26).
 *
 * These live here rather than in the feature that shows the data, because data is not
 * UI-scoped: accounts are read by the accounts screen, the dashboard and the transaction
 * form. A feature that owned its own repository would become a dependency of every screen
 * that reads it — see `docs/adr/0006-centralized-data-layer.md`.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindPendingChangesMonitor(
        impl: RoomPendingChangesMonitor,
    ): PendingChangesMonitor
}

/**
 * The set the sync worker iterates over.
 *
 * Dagger defines no iteration order for a multibinding, so ordering is not left to this
 * file: every [Syncable] declares its `table`, and `SyncWorker` sorts on `SyncTable`'s
 * ordinal — profile, then accounts and categories, then the rows that reference them — so
 * a pulled transaction never lands before the account it points at.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class SyncableModule {

    @Binds
    @IntoSet
    abstract fun bindProfileSyncable(impl: ProfileRepositoryImpl): Syncable

    @Binds
    @IntoSet
    abstract fun bindAccountSyncable(impl: AccountRepositoryImpl): Syncable

    @Binds
    @IntoSet
    abstract fun bindCategorySyncable(impl: CategoryRepositoryImpl): Syncable

    @Binds
    @IntoSet
    abstract fun bindTransactionSyncable(impl: TransactionRepositoryImpl): Syncable

    @Binds
    @IntoSet
    abstract fun bindBudgetSyncable(impl: BudgetRepositoryImpl): Syncable

    @Binds
    @IntoSet
    abstract fun bindGoalSyncable(impl: GoalRepositoryImpl): Syncable
}
