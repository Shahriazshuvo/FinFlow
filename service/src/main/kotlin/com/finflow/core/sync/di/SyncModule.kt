package com.finflow.core.sync.di

import com.finflow.core.data.sync.SyncTrigger
import com.finflow.core.data.sync.Synchronizer
import com.finflow.core.domain.repository.SyncRepository
import com.finflow.core.sync.DataStoreSynchronizer
import com.finflow.core.sync.SyncManager
import com.finflow.core.sync.SyncRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSynchronizer(impl: DataStoreSynchronizer): Synchronizer

    @Binds
    @Singleton
    abstract fun bindSyncTrigger(impl: SyncManager): SyncTrigger

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository
}
