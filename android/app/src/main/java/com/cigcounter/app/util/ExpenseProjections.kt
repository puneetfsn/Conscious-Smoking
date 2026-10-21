package com.cigcounter.app.util

/**
 * Expected expense projection calculations based on user's daily consumption patterns.
 * 100% offline, pure arithmetic with zero online connectivity needed.
 */
data class ExpenseProjection(
    val dailyExpense: Double,
    val weeklyExpense: Double,
    val monthlyExpense: Double,
    val yearlyExpense: Double,
    val savingsIfReducedByTwoPerDay: Double,
    val savingsIfReducedByHalf: Double,
    val fullQuitAnnualSavings: Double
)

object ExpenseCalculator {

    /**
     * Projects forward costs based on daily average count and unit price.
     */
    fun computeProjections(dailyCigCount: Double, pricePerUnit: Double): ExpenseProjection {
        val safeCount = dailyCigCount.coerceAtLeast(0.0)
        val safePrice = pricePerUnit.coerceAtLeast(0.0)

        val daily = safeCount * safePrice
        val weekly = daily * 7.0
        val monthly = daily * 30.0
        val yearly = daily * 365.0

        val reduceByTwoPerDay = (2.0.coerceAtMost(safeCount)) * safePrice * 365.0
        val reduceByHalf = yearly * 0.50
        val fullQuitSavings = yearly

        return ExpenseProjection(
            dailyExpense = daily,
            weeklyExpense = weekly,
            monthlyExpense = monthly,
            yearlyExpense = yearly,
            savingsIfReducedByTwoPerDay = reduceByTwoPerDay,
            savingsIfReducedByHalf = reduceByHalf,
            fullQuitAnnualSavings = fullQuitSavings
        )
    }
}
