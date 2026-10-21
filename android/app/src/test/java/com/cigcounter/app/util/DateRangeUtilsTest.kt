package com.cigcounter.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class DateRangeUtilsTest {

    private val fixedZoneId = ZoneId.of("UTC")

    @Test
    fun startOfToday_isMidnightUtc() {
        val startOfTodayMillis = DateRangeUtils.getStartOfTodayMillis(fixedZoneId)
        val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startOfTodayMillis), fixedZoneId)

        assertEquals(0, zdt.hour)
        assertEquals(0, zdt.minute)
        assertEquals(0, zdt.second)
        assertEquals(0, zdt.nano)
        assertEquals(LocalDate.now(fixedZoneId), zdt.toLocalDate())
    }

    @Test
    fun weekStartMillis_coversExactly7DaysIncludingToday() {
        val weekStartMillis = DateRangeUtils.getWeekStartMillis(fixedZoneId)
        val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(weekStartMillis), fixedZoneId)

        val expectedStartDate = LocalDate.now(fixedZoneId).minusDays(6)
        assertEquals(expectedStartDate, zdt.toLocalDate())
        assertEquals(0, zdt.hour)
        assertEquals(0, zdt.minute)
    }

    @Test
    fun monthStartMillis_coversExactly30DaysIncludingToday() {
        val monthStartMillis = DateRangeUtils.getMonthStartMillis(fixedZoneId)
        val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(monthStartMillis), fixedZoneId)

        val expectedStartDate = LocalDate.now(fixedZoneId).minusDays(29)
        assertEquals(expectedStartDate, zdt.toLocalDate())
        assertEquals(0, zdt.hour)
        assertEquals(0, zdt.minute)
    }

    @Test
    fun dailyTimeWindows_producesChronologicalDays() {
        val windows7 = DateRangeUtils.getDailyTimeWindows(7, fixedZoneId)
        assertEquals(7, windows7.size)

        val today = LocalDate.now(fixedZoneId)
        assertEquals(today.minusDays(6), windows7.first().date)
        assertEquals(today, windows7.last().date)

        for (i in 0 until windows7.size - 1) {
            assertTrue(windows7[i].startMillis < windows7[i + 1].startMillis)
            assertTrue(windows7[i].endMillis < windows7[i + 1].startMillis)
        }
    }

    @Test
    fun millisecondsUntilNextMidnight_isPositiveAndLessThan24Hours() {
        val millisUntilMidnight = DateRangeUtils.getMillisecondsUntilNextMidnight(fixedZoneId)
        assertTrue("Duration must be > 0", millisUntilMidnight > 0)
        assertTrue("Duration must be <= 24h", millisUntilMidnight <= 86_400_000L + 5000L)
    }
}
