package com.cigcounter.app.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Calculation and formatting utilities for units and counts.
 * Strictly adheres to generic 'units' with NO currency symbols.
 */
object CalculationUtils {

    private val symbols = DecimalFormatSymbols(Locale.US)
    private val decimalFormat = DecimalFormat("#,##0.##", symbols)
    private val wholeNumberFormat = DecimalFormat("#,##0", symbols)

    /**
     * Calculates total spent in units given a count and price per cigarette.
     */
    fun calculateSpent(count: Int, pricePerUnit: Double): Double {
        if (count <= 0 || pricePerUnit <= 0.0) return 0.0
        return count * pricePerUnit
    }

    /**
     * Formats unit amounts cleanly without trailing zeros if whole, and never includes currency symbols.
     * E.g. 10.0 -> "10", 7.5 -> "7.5", 1245.25 -> "1,245.25"
     */
    fun formatUnits(units: Double): String {
        return if (units % 1.0 == 0.0) {
            wholeNumberFormat.format(units)
        } else {
            decimalFormat.format(units)
        }
    }

    /**
     * Validates user price input string.
     */
    fun validatePrice(input: String): PriceValidationResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return PriceValidationResult.Error("Price cannot be empty.")
        }
        val parsed = trimmed.toDoubleOrNull()
            ?: return PriceValidationResult.Error("Please enter a valid numeric value.")

        if (parsed <= 0.0) {
            return PriceValidationResult.Error("Price must be greater than zero.")
        }
        if (parsed > 100000.0) {
            return PriceValidationResult.Error("Price exceeds maximum limit (100,000 units).")
        }

        return PriceValidationResult.Success(parsed)
    }
}

sealed class PriceValidationResult {
    data class Success(val value: Double) : PriceValidationResult()
    data class Error(val message: String) : PriceValidationResult()
}
