package com.finflow.core.sync

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.finflow.core.data.sync.SyncTrigger
import com.finflow.core.sync.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The one place that knows about WorkManager. Repositories only see [SyncTrigger], so the
 * write path never depends on a scheduling API.
 */
@Singleton
internal class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : SyncTrigger {

    private val workManager get() = WorkManager.getInstance(context)

    val isSyncing: Flow<Boolean> = workManager
        .getWorkInfosForUniqueWorkFlow(SyncWorker.ONE_TIME_WORK_NAME)
        .map { infos -> infos.any { it.state == WorkInfo.State.RUNNING } }

    override fun requestSync() {
        workManager.enqueueUniqueWork(
            SyncWorker.ONE_TIME_WORK_NAME,
            SyncWorker.ExistingOneTimePolicy,
            SyncWorker.oneTimeRequest(),
        )
    }

    /** Called once from the Application so background sync survives process death. */
    fun schedulePeriodicSync() {
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.PERIODIC_WORK_NAME,
            SyncWorker.ExistingPeriodicPolicy,
            SyncWorker.periodicRequest(),
        )
    }
}
