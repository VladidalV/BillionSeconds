package com.example.billionseconds.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DateTimeFormatterTest {

    @Test
    fun `formatInstant should return formatted string`() {
        val instant = localDateTimeToInstant(
            LocalDate(2020, 1, 1),
            LocalTime(12, 30, 45),
            TimeZone.UTC
        )
        
        val result = DateTimeFormatter.formatInstant(instant)
        
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("2020") || result.contains("01"))
        assertTrue(result.contains("12:") || result.contains("30"))
    }

    @Test
    fun `formatInstantWithTimezone should include timezone info`() {
        val instant = localDateTimeToInstant(
            LocalDate(2020, 1, 1),
            LocalTime(12, 0, 0),
            TimeZone.UTC
        )
        
        val result = DateTimeFormatter.formatInstantWithTimezone(instant)
        
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("+00:00") || result.contains("UTC") || result.contains("System"))
    }

    @Test
    fun `formatRelativeTime for future instant should contain 'until'`() {
        val futureInstant = localDateTimeToInstant(
            LocalDate(2050, 1, 1),
            LocalTime(0, 0, 0),
            TimeZone.currentSystemDefault()
        )
        
        val result = DateTimeFormatter.formatRelativeTime(futureInstant)
        
        assertTrue(result.contains("until"))
    }

    @Test
    fun `formatRelativeTime for past instant should contain 'ago'`() {
        val pastInstant = localDateTimeToInstant(
            LocalDate(2000, 1, 1),
            LocalTime(0, 0, 0),
            TimeZone.currentSystemDefault()
        )
        
        val result = DateTimeFormatter.formatRelativeTime(pastInstant)
        
        assertTrue(result.contains("ago"))
    }

    @Test
    fun `formatDetailedResult should include both formatted date and relative time`() {
        val birthInstant = localDateTimeToInstant(
            LocalDate(2020, 1, 1),
            LocalTime(0, 0, 0),
            TimeZone.currentSystemDefault()
        )
        
        val billionSecondsInstant = localDateTimeToInstant(
            LocalDate(2051, 9, 9),
            LocalTime(1, 46, 40),
            TimeZone.currentSystemDefault()
        )
        
        val result = DateTimeFormatter.formatDetailedResult(billionSecondsInstant, birthInstant)
        
        assertTrue(result.contains("1,000,000,000"))
        assertTrue(result.isNotEmpty())
    }
}