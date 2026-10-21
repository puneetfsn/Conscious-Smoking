package com.cigcounter.app.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cigcounter.app.CigApp
import com.cigcounter.app.MainActivity
import com.cigcounter.app.R
import com.cigcounter.app.util.CalculationUtils
import com.cigcounter.app.util.DateRangeUtils
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Executes every evening at 9:00 PM completely offline using WorkManager.
 * Compares today's count with yesterday's count and displays a supportive notification.
 */
class EveningSummaryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as CigApp
        val settings = app.preferencesRepository.getSettings()

        if (!settings.eveningNotificationEnabled) {
            schedule(applicationContext)
            return Result.success()
        }

        val startOfToday = DateRangeUtils.getStartOfTodayMillis()
        val startOfYesterday = DateRangeUtils.getDaysAgoStartOfDayMillis(1)

        val todayCount = app.repository.getSmokedCountFrom(startOfToday)
        val yesterdayCount = app.repository.getSmokedCountBetween(startOfYesterday, startOfToday)
        val todaySpent = CalculationUtils.calculateSpent(todayCount, settings.pricePerCigarette)

        val title = "Evening Habit Summary"
        val message = when {
            todayCount == 0 -> "Smoke-free day! 0 cigarettes logged. Incredible progress! 🎉"
            todayCount < yesterdayCount -> "Today: $todayCount cigs (${CalculationUtils.formatUnits(todaySpent)} units). That's ${yesterdayCount - todayCount} less than yesterday! 👏"
            todayCount == yesterdayCount -> "Today: $todayCount cigs (${CalculationUtils.formatUnits(todaySpent)} units). Consistent with yesterday."
            else -> "Today: $todayCount cigs (${CalculationUtils.formatUnits(todaySpent)} units). Rest well; tomorrow is a fresh start."
        }

        showNotification(applicationContext, title, message)

        // Reschedule for next 9 PM
        schedule(applicationContext)

        return Result.success()
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "evening_summary_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Evening Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily 9 PM habit and spend reflection (100% offline)"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_cig_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(9001, notification)
    }

    companion object {
        private const val WORK_NAME = "smokeledger_evening_summary_worker"

        fun schedule(context: Context) {
            val now = ZonedDateTime.now()
            var targetTime = now.withHour(21).withMinute(0).withSecond(0).withNano(0)
            if (now.isAfter(targetTime)) {
                targetTime = targetTime.plusDays(1)
            }
            val delay = Duration.between(now, targetTime).toMillis()

            val workRequest = OneTimeWorkRequestBuilder<EveningSummaryWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
