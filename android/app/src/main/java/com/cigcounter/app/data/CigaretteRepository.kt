package com.cigcounter.app.data

import com.cigcounter.app.util.CalculationUtils
import com.cigcounter.app.util.DateRangeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class HabitSummary(
    val todayCount: Int = 0,
    val todaySkippedCount: Int = 0,
    val weekCount: Int = 0,
    val monthCount: Int = 0,
    val pricePerUnit: Double = 10.0,
    val todaySpentUnits: Double = 0.0,
    val todaySkippedSavedUnits: Double = 0.0,
    val weekSpentUnits: Double = 0.0,
    val monthSpentUnits: Double = 0.0,
    val todaySavedUnits: Double = 0.0,
    val lastSmokedTimestamp: Long? = null,
    val paceMonthlyCigs: Double = 0.0,
    val paceYearlyCigs: Double = 0.0,
    val paceYearlyUnits: Double = 0.0,
    val consecutiveSmokingDays: Int = 0,
    val smokedToday: Boolean = false
)

data class HourlyTriggerData(
    val hourCounts: List<Int> = List(24) { 0 },
    val peakHour: Int? = null,
    val peakCount: Int = 0,
    val totalSmoked: Int = 0
)

data class DayStat(
    val date: LocalDate,
    val label: String,
    val count: Int,
    val skippedCount: Int,
    val spentUnits: Double
)

class CigaretteRepository(
    private val dao: CigaretteDao,
    private val preferencesRepository: UserPreferencesRepository
) {

    val settingsFlow: Flow<UserSettings> = preferencesRepository.settingsFlow

    suspend fun logCigarette(): Long {
        return dao.insert(CigaretteLog(timestampMillis = System.currentTimeMillis(), isSkipped = false))
    }

    suspend fun logSkippedCigarette(): Long {
        return dao.insert(CigaretteLog(timestampMillis = System.currentTimeMillis(), isSkipped = true))
    }

    suspend fun logManual(timestampMillis: Long, isSkipped: Boolean = false): Long {
        return dao.insert(CigaretteLog(timestampMillis = timestampMillis, isSkipped = isSkipped))
    }

    suspend fun undoLast(): Boolean {
        return dao.deleteLatest() > 0
    }

    suspend fun deleteAll(): Int {
        return dao.deleteAll()
    }

    fun getLatestSmokedLogFlow(): Flow<CigaretteLog?> = dao.getLatestSmokedFlow()

    fun getRecentLogsFlow(limit: Int = 50): Flow<List<CigaretteLog>> = dao.getRecentLogs(limit)

    suspend fun getSmokedCountFrom(fromMillis: Long): Int = dao.countSmokedFrom(fromMillis)

    suspend fun getSmokedCountBetween(from: Long, to: Long): Int = dao.countSmokedBetween(from, to)

    fun getTotalSmokedCountFlow(): Flow<Int> = dao.countTotalSmokedFlow()

    suspend fun getTotalSmokedCount(): Int = dao.countTotalSmoked()

    fun getHabitSummaryFlow(): Flow<HabitSummary> {
        val startOfToday = DateRangeUtils.getStartOfTodayMillis()
        val startOfWeek = DateRangeUtils.getWeekStartMillis()
        val startOfMonth = DateRangeUtils.getMonthStartMillis()
        val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24L * 3600L * 1000L)

        val todaySmokedFlow = dao.countSmokedFromFlow(startOfToday)
        val todaySkippedFlow = dao.countSkippedFromFlow(startOfToday)
        val weekSmokedFlow = dao.countSmokedFromFlow(startOfWeek)
        val monthSmokedFlow = dao.countSmokedFromFlow(startOfMonth)
        val latestSmokedFlow = dao.getLatestSmokedFlow()
        val settingsFlow = preferencesRepository.settingsFlow
        val recentLogsFlow = dao.getLogsFromFlow(ninetyDaysAgo)

        return combine(
            todaySmokedFlow,
            todaySkippedFlow,
            weekSmokedFlow,
            monthSmokedFlow,
            latestSmokedFlow,
            settingsFlow,
            recentLogsFlow
        ) { args: Array<Any?> ->
            val todaySmoked = args[0] as Int
            val todaySkipped = args[1] as Int
            val weekSmoked = args[2] as Int
            val monthSmoked = args[3] as Int
            val latestSmoked = args[4] as CigaretteLog?
            val settings = args[5] as UserSettings
            @Suppress("UNCHECKED_CAST")
            val recentLogs = args[6] as List<CigaretteLog>

            val price = settings.pricePerCigarette
            val todaySpent = CalculationUtils.calculateSpent(todaySmoked, price)
            val todaySkippedSaved = CalculationUtils.calculateSpent(todaySkipped, price)
            val weekSpent = CalculationUtils.calculateSpent(weekSmoked, price)
            val monthSpent = CalculationUtils.calculateSpent(monthSmoked, price)

            val dailyAvg = weekSmoked / 7.0
            val paceMonthCigs = dailyAvg * 30.0
            val paceYearCigs = dailyAvg * 365.0
            val paceYearUnits = paceYearCigs * price

            val zone = java.time.ZoneId.systemDefault()
            val smokedDates = recentLogs
                .filter { !it.isSkipped }
                .map { java.time.Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate() }
                .toSet()

            val today = java.time.LocalDate.now(zone)
            val hasSmokedToday = todaySmoked > 0 || smokedDates.contains(today)
            var streak = 0
            if (hasSmokedToday) {
                streak = 1
                var checkDate = today.minusDays(1)
                while (smokedDates.contains(checkDate)) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                }
            } else {
                var checkDate = today.minusDays(1)
                while (smokedDates.contains(checkDate)) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                }
            }

            HabitSummary(
                todayCount = todaySmoked,
                todaySkippedCount = todaySkipped,
                weekCount = weekSmoked,
                monthCount = monthSmoked,
                pricePerUnit = price,
                todaySpentUnits = todaySpent,
                todaySkippedSavedUnits = todaySkippedSaved,
                weekSpentUnits = weekSpent,
                monthSpentUnits = monthSpent,
                todaySavedUnits = todaySpent,
                lastSmokedTimestamp = latestSmoked?.timestampMillis,
                paceMonthlyCigs = paceMonthCigs,
                paceYearlyCigs = paceYearCigs,
                paceYearlyUnits = paceYearUnits,
                consecutiveSmokingDays = streak,
                smokedToday = hasSmokedToday
            )
        }
    }

    suspend fun getHabitSummarySync(): HabitSummary {
        val startOfToday = DateRangeUtils.getStartOfTodayMillis()
        val startOfWeek = DateRangeUtils.getWeekStartMillis()
        val startOfMonth = DateRangeUtils.getMonthStartMillis()
        val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24L * 3600L * 1000L)

        val todaySmoked = dao.countSmokedFrom(startOfToday)
        val todaySkipped = dao.countSkippedFrom(startOfToday)
        val weekSmoked = dao.countSmokedFrom(startOfWeek)
        val monthSmoked = dao.countSmokedFrom(startOfMonth)
        val latestSmoked = dao.getLatestSmoked()
        val settings = preferencesRepository.getSettings()
        val recentLogs = dao.getLogsFrom(ninetyDaysAgo)

        val price = settings.pricePerCigarette
        val todaySpent = CalculationUtils.calculateSpent(todaySmoked, price)
        val todaySkippedSaved = CalculationUtils.calculateSpent(todaySkipped, price)
        val weekSpent = CalculationUtils.calculateSpent(weekSmoked, price)
        val monthSpent = CalculationUtils.calculateSpent(monthSmoked, price)

        val dailyAvg = weekSmoked / 7.0
        val paceMonthCigs = dailyAvg * 30.0
        val paceYearCigs = dailyAvg * 365.0
        val paceYearUnits = paceYearCigs * price

        val zone = java.time.ZoneId.systemDefault()
        val smokedDates = recentLogs
            .filter { !it.isSkipped }
            .map { java.time.Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate() }
            .toSet()

        val today = java.time.LocalDate.now(zone)
        val hasSmokedToday = todaySmoked > 0 || smokedDates.contains(today)
        var streak = 0
        if (hasSmokedToday) {
            streak = 1
            var checkDate = today.minusDays(1)
            while (smokedDates.contains(checkDate)) {
                streak++
                checkDate = checkDate.minusDays(1)
            }
        } else {
            var checkDate = today.minusDays(1)
            while (smokedDates.contains(checkDate)) {
                streak++
                checkDate = checkDate.minusDays(1)
            }
        }

        return HabitSummary(
            todayCount = todaySmoked,
            todaySkippedCount = todaySkipped,
            weekCount = weekSmoked,
            monthCount = monthSmoked,
            pricePerUnit = price,
            todaySpentUnits = todaySpent,
            todaySkippedSavedUnits = todaySkippedSaved,
            weekSpentUnits = weekSpent,
            monthSpentUnits = monthSpent,
            todaySavedUnits = todaySpent,
            lastSmokedTimestamp = latestSmoked?.timestampMillis,
            paceMonthlyCigs = paceMonthCigs,
            paceYearlyCigs = paceYearCigs,
            paceYearlyUnits = paceYearUnits,
            consecutiveSmokingDays = streak,
            smokedToday = hasSmokedToday
        )
    }

    fun getHourlyTriggerFlow(daysCount: Int = 30): Flow<HourlyTriggerData> {
        val earliestMillis = System.currentTimeMillis() - (daysCount.toLong() * 24L * 3600L * 1000L)
        val zone = java.time.ZoneId.systemDefault()

        return dao.getLogsFromFlow(earliestMillis).map { logs ->
            val smokedLogs = logs.filter { !it.isSkipped }
            val counts = IntArray(24) { 0 }

            for (log in smokedLogs) {
                val hour = java.time.Instant.ofEpochMilli(log.timestampMillis)
                    .atZone(zone)
                    .hour
                if (hour in 0..23) {
                    counts[hour]++
                }
            }

            var peakH: Int? = null
            var peakC = 0
            for (h in 0 until 24) {
                if (counts[h] > peakC) {
                    peakC = counts[h]
                    peakH = h
                }
            }

            HourlyTriggerData(
                hourCounts = counts.toList(),
                peakHour = if (peakC > 0) peakH else null,
                peakCount = peakC,
                totalSmoked = smokedLogs.size
            )
        }
    }

    fun getDailyStatsFlow(daysCount: Int): Flow<List<DayStat>> {
        val windows = DateRangeUtils.getDailyTimeWindows(daysCount)
        val earliestMillis = windows.first().startMillis

        return combine(
            dao.getLogsFromFlow(earliestMillis),
            preferencesRepository.pricePerCigaretteFlow
        ) { logs, price ->
            windows.map { window ->
                val dayLogs = logs.filter { it.timestampMillis in window.startMillis..window.endMillis }
                val countSmoked = dayLogs.count { !it.isSkipped }
                val countSkipped = dayLogs.count { it.isSkipped }

                val label = if (daysCount <= 7) {
                    window.date.dayOfWeek.name.take(3)
                } else {
                    "${window.date.monthValue}/${window.date.dayOfMonth}"
                }
                DayStat(
                    date = window.date,
                    label = label,
                    count = countSmoked,
                    skippedCount = countSkipped,
                    spentUnits = CalculationUtils.calculateSpent(countSmoked, price)
                )
            }
        }
    }

    suspend fun getAllLogs(): List<CigaretteLog> = dao.getAllLogs()

    suspend fun exportBackupJson(): String {
        val allLogs = dao.getAllLogs()
        val settings = preferencesRepository.getSettings()

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAtMillis", System.currentTimeMillis())
        root.put("pricePerCigarette", settings.pricePerCigarette)
        root.put("wishlistName", settings.wishlistName)
        root.put("wishlistCost", settings.wishlistCost)
        root.put("isDarkMode", settings.isDarkMode)

        val array = JSONArray()
        allLogs.forEach { log ->
            val obj = JSONObject()
            obj.put("timestampMillis", log.timestampMillis)
            obj.put("isSkipped", log.isSkipped)
            array.put(obj)
        }
        root.put("logs", array)

        return root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean {
        return runCatching {
            val root = JSONObject(jsonString)
            val logsArray = root.getJSONArray("logs")
            val restoredLogs = mutableListOf<CigaretteLog>()

            for (i in 0 until logsArray.length()) {
                val item = logsArray.getJSONObject(i)
                restoredLogs.add(
                    CigaretteLog(
                        timestampMillis = item.getLong("timestampMillis"),
                        isSkipped = item.optBoolean("isSkipped", false)
                    )
                )
            }

            if (root.has("pricePerCigarette")) {
                preferencesRepository.setPricePerCigarette(root.getDouble("pricePerCigarette"))
            }
            if (root.has("wishlistName") && root.has("wishlistCost")) {
                preferencesRepository.setWishlist(root.getString("wishlistName"), root.getDouble("wishlistCost"))
            }
            if (root.has("isDarkMode")) {
                preferencesRepository.setDarkMode(root.getBoolean("isDarkMode"))
            }

            dao.insertAll(restoredLogs)
            true
        }.getOrDefault(false)
    }
}
