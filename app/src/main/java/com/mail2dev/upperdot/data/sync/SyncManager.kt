package com.mail2dev.upperdot.data.sync

import android.content.Context
import androidx.work.*
import com.mail2dev.upperdot.data.worker.DriveSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class SyncManager(private val context: Context) {

    val syncStatus: Flow<Boolean> = WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkFlow("ImmediateDriveSync")
        .map { it.any { info -> info.state == WorkInfo.State.RUNNING || info.state == WorkInfo.State.ENQUEUED } }

    val periodicSyncStatus: Flow<Boolean> = WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkFlow("PeriodicDriveSync")
        .map { it.any { info -> info.state == WorkInfo.State.RUNNING } }

    fun schedulePeriodicSync(intervalHours: Long, wifiOnly: Boolean) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<DriveSyncWorker>(intervalHours, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PeriodicDriveSync",
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }

    fun startImmediateSync(wifiOnly: Boolean = false) {
        // Enforce UNMETERED (Wi-Fi) if wifiOnly preference is enabled
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<DriveSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "ImmediateDriveSync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    fun cancelPeriodicSync() {
        WorkManager.getInstance(context).cancelUniqueWork("PeriodicDriveSync")
    }
}