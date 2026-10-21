package com.cigcounter.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculationUtilsTest {

    @Test
    fun calculateSpent_withZeroCount_returnsZero() {
        assertEquals(0.0, CalculationUtils.calculateSpent(0, 10.0), 0.001)
    }

    @Test
    fun calculateSpent_withValidInputs_returnsCorrectProduct() {
        assertEquals(50.0, CalculationUtils.calculateSpent(5, 10.0), 0.001)
        assertEquals(37.5, CalculationUtils.calculateSpent(5, 7.5), 0.001)
        assertEquals(22.5, CalculationUtils.calculateSpent(3, 7.5), 0.001)
    }

    @Test
    fun formatUnits_neverIncludesCurrencySymbol() {
        val formattedWhole = CalculationUtils.formatUnits(10.0)
        val formattedDecimal = CalculationUtils.formatUnits(7.5)
        val formattedLarge = CalculationUtils.formatUnits(1250.0)

        assertEquals("10", formattedWhole)
        assertEquals("7.5", formattedDecimal)
        assertEquals("1,250", formattedLarge)

        assertFalse(formattedWhole.contains("$"))
        assertFalse(formattedWhole.contains("€"))
        assertFalse(formattedDecimal.contains("$"))
        assertFalse(formattedLarge.contains("$"))
    }

    @Test
    fun validatePrice_withEmpty_returnsError() {
        val result = CalculationUtils.validatePrice("   ")
        assertTrue(result is PriceValidationResult.Error)
        assertEquals("Price cannot be empty.", (result as PriceValidationResult.Error).message)
    }

    @Test
    fun validatePrice_withNonNumeric_returnsError() {
        val result = CalculationUtils.validatePrice("abc")
        assertTrue(result is PriceValidationResult.Error)
        assertEquals("Please enter a valid numeric value.", (result as PriceValidationResult.Error).message)
    }

    @Test
    fun validatePrice_withNegativeOrZero_returnsError() {
        val zeroResult = CalculationUtils.validatePrice("0")
        assertTrue(zeroResult is PriceValidationResult.Error)

        val negativeResult = CalculationUtils.validatePrice("-5.5")
        assertTrue(negativeResult is PriceValidationResult.Error)
    }

    @Test
    fun validatePrice_withValidDecimal_returnsSuccess() {
        val result = CalculationUtils.validatePrice("7.5")
        assertTrue(result is PriceValidationResult.Success)
        assertEquals(7.5, (result as PriceValidationResult.Success).value, 0.001)

        val integerResult = CalculationUtils.validatePrice("12")
        assertTrue(integerResult is PriceValidationResult.Success)
        assertEquals(12.0, (integerResult as PriceValidationResult.Success).value, 0.001)
    }
}
