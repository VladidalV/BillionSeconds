package com.example.billionseconds.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class BillionSecondsCalculatorTest {

    @Test
    fun `calculateBillionSeconds should calculate successfully`() {
        val birthDate = LocalDate(2020, 1, 1)
        val birthTime = LocalTime(0, 0, 0)
        
        val resultInstant = BillionSecondsCalculator.calculateBillionSeconds(birthDate, birthTime)
        
        assertTrue(resultInstant.toEpochMilliseconds() > 0, "Should return valid instant")
    }

    @Test
    fun `isBillionSecondsReached should return false for future date`() {
        val futureDate = LocalDate(2050, 1, 1)
        val futureTime = LocalTime(0, 0, 0)
        
        val result = BillionSecondsCalculator.isBillionSecondsReached(futureDate, futureTime)
        
        assertFalse(result, "Billion seconds should not be reached for future dates")
    }

    @Test
    fun `getSecondsUntilBillionSeconds should return positive value for future dates`() {
        val futureDate = LocalDate(2050, 1, 1)
        val futureTime = LocalTime(0, 0, 0)
        
        val secondsUntil = BillionSecondsCalculator.getSecondsUntilBillionSeconds(futureDate, futureTime)
        
        assertTrue(secondsUntil > 0, "Should have positive seconds until billion seconds")
    }

    @Test
    fun `calculateBillionSeconds should handle different dates`() {
        val testCases = listOf(
            Pair(LocalDate(2020, 1, 1), LocalTime(0, 0, 0)),
            Pair(LocalDate(1990, 6, 15), LocalTime(12, 30, 45)),
            Pair(LocalDate(2000, 12, 31), LocalTime(23, 59, 59))
        )
        
        for ((date, time) in testCases) {
            val result = BillionSecondsCalculator.calculateBillionSeconds(date, time)
            assertTrue(result.toEpochMilliseconds() > 0, 
                "Billion seconds calculation failed for date: $date, time: $time")
        }
    }
}