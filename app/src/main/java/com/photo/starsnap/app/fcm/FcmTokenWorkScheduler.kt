package com.photo.starsnap.app.fcm

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

internal interface FcmTokenWorkScheduler {
    fun enqueue()
    fun cancel()
}

@Singleton
internal class WorkManagerFcmTokenWorkScheduler @Inject constructor(
    @ApplicationContext context: Context,
) : FcmTokenWorkScheduler {
    private val workManager = WorkManager.getInstance(context)

    override fun enqueue() {
        val request = OneTimeWorkRequestBuilder<FcmTokenSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS,
            )
            .build()

        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    override fun cancel() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "sync-fcm-registration-token"
    }
}
