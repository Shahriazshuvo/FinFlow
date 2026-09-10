package com.finflow.core.common.time

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/**
 * Nothing calls `Instant.now()` directly. Every timestamp the app writes goes through an
 * injected [Clock], so sync-conflict and "pending write" tests can pin time.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object ClockModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()
}
