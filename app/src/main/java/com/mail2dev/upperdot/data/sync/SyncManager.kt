package com.mail2dev.upperdot.data.sync

import android.content.Context
import androidx.work.*
import com.mail2dev.upperdot.data.worker.DriveSyncWorker
import java.util.concurrent.TimeUnit

class SyncManager(private val context: Context) {

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

    fun startImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
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
