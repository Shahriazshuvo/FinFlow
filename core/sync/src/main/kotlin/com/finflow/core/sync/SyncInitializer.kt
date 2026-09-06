package com.finflow.core.sync

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Entry point the Application calls once on startup. Keeps `:app` free of any WorkManager
 * import — it just asks sync to start itself.
 */
@Singleton
class SyncInitializer @Inject internal constructor(
    private val syncManager: SyncManager,
) {
    fun initialize() {
        syncManager.schedulePeriodicSync()
    }
}
