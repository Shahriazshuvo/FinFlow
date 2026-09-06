package com.finflow.core.sync.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.finflow.core.common.dispatcher.DispatcherProvider
import com.finflow.core.data.sync.Syncable
import com.finflow.core.data.sync.Synchronizer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Runs every [Syncable] repository in dependency order. A table that fails leaves its rows
 * pending and the whole run is retried with backoff — nothing is ever dropped, and the UI
 * keeps reading Room in the meantime (APP_SPEC.md §12).
 */
@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncables: Set<@JvmSuppressWildcards Syncable>,
    private val synchronizer: Synchronizer,
    private val dispatchers: DispatcherProvider,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(dispatchers.io) {
        val allSucceeded = syncables.fold(initial = true) { acc, syncable ->
            val succeeded = runCatching { syncable.syncWith(synchronizer) }
                .onFailure { Log.w(TAG, "Sync failed for ${syncable::class.simpleName}", it) }
                .getOrDefault(false)
            acc && succeeded
        }

        if (allSucceeded) {
            Log.i(TAG, "Sync completed")
            Result.success()
        } else {
            Log.w(TAG, "Sync incomplete, scheduling retry")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "SyncWorker"

        const val ONE_TIME_WORK_NAME = "finflow_sync_now"
        const val PERIODIC_WORK_NAME = "finflow_sync_periodic"

        private const val BACKOFF_SECONDS = 30L
        private const val PERIODIC_INTERVAL_HOURS = 6L

        val ExistingOneTimePolicy = ExistingWorkPolicy.KEEP
        val ExistingPeriodicPolicy = ExistingPeriodicWorkPolicy.KEEP

        private val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun oneTimeRequest() = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()

        fun periodicRequest() = PeriodicWorkRequestBuilder<SyncWorker>(
            PERIODIC_INTERVAL_HOURS,
            TimeUnit.HOURS,
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()
    }
}
