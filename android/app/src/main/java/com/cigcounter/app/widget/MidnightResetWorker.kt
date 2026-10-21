package com.cigcounter.app.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cigcounter.app.util.DateRangeUtils
import java.util.concurrent.TimeUnit

/**
 * Worker executed at midnight to refresh widgets, ensuring the "Today" counter
 * cleanly resets to 0.
 *
 * Why WorkManager over AlarmManager:
 * 1. Battery Efficiency: WorkManager adheres to modern Android Doze mode and system constraints without draining battery.
 * 2. Guaranteed Execution: Survives process death and respects OS background execution limits.
 * 3. Permission Simplicity: AlarmManager on Android 12+ requires exact alarm permissions (SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM)
 *    which can be revoked, restricted by OEM battery optimizers, or require Google Play declaration justifications.
 *    For a habit widget where a precision of a few seconds at midnight is fine, WorkManager is more reliable and zero-permission.
 */
class MidnightResetWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Refresh all home screen widgets with fresh counts
        CigWidgetReceiver.updateAll(applicationContext)

        // Reschedule for the subsequent midnight
        schedule(applicationContext)

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "cig_counter_midnight_reset_worker"

        /**
         * Computes the initial delay until 00:00:01 and enqueues a uniquely named WorkRequest.
         */
        fun schedule(context: Context) {
            val delayMillis = DateRangeUtils.getMillisecondsUntilNextMidnight()
            val workRequest = OneTimeWorkRequestBuilder<MidnightResetWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
