package com.cigcounter.app.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Date and time range utilities using java.time.
 * All computations respect the user's local timezone.
 */
object DateRangeUtils {

    /**
     * Timestamp (in epoch millis) of local midnight (00:00:00) of today.
     */
    fun getStartOfTodayMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return LocalDate.now(zoneId)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Timestamp of start of day for N days ago.
     * E.g. daysBack = 1 gives start of yesterday.
     */
    fun getDaysAgoStartOfDayMillis(daysBack: Long, zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return LocalDate.now(zoneId)
            .minusDays(daysBack)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Start timestamp for the last 7 days including today (today minus 6 days at 00:00).
     */
    fun getWeekStartMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return getDaysAgoStartOfDayMillis(6, zoneId)
    }

    /**
     * Start timestamp for the last 30 days including today (today minus 29 days at 00:00).
     */
    fun getMonthStartMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return getDaysAgoStartOfDayMillis(29, zoneId)
    }

    /**
     * Returns the epoch millis start and end for each day in a range of [daysCount] days,
     * starting from (today - (daysCount - 1)) up to and including today.
     */
    fun getDailyTimeWindows(daysCount: Int, zoneId: ZoneId = ZoneId.systemDefault()): List<DailyTimeWindow> {
        val today = LocalDate.now(zoneId)
        val windows = mutableListOf<DailyTimeWindow>()

        for (i in (daysCount - 1) downTo 0) {
            val date = today.minusDays(i.toLong())
            val startMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
            windows.add(DailyTimeWindow(date = date, startMillis = startMillis, endMillis = endMillis))
        }
        return windows
    }

    /**
     * Calculates the duration in milliseconds from now until the next upcoming midnight (00:00:01).
     * Used by WorkManager to trigger widget reset at midnight.
     */
    fun getMillisecondsUntilNextMidnight(zoneId: ZoneId = ZoneId.systemDefault()): Long {
        val now = ZonedDateTime.now(zoneId)
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zoneId).plusSeconds(1)
        val duration = Duration.between(now, nextMidnight)
        val millis = duration.toMillis()
        return if (millis > 0) millis else 1000L
    }
}

data class DailyTimeWindow(
    val date: LocalDate,
    val startMillis: Long,
    val endMillis: Long
)
