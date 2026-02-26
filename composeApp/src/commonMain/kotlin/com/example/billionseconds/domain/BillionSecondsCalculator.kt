package com.example.billionseconds.domain

import com.example.billionseconds.util.getCurrentInstant
import com.example.billionseconds.util.localDateTimeToInstant
import com.example.billionseconds.util.plusSeconds
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

object BillionSecondsCalculator {

    private const val BILLION_SECONDS = 1_000_000_000L

    fun calculateBillionSeconds(
        birthDate: LocalDate,
        birthTime: LocalTime
    ): Instant {
        val timeZone = TimeZone.currentSystemDefault()
        val birthInstant = localDateTimeToInstant(birthDate, birthTime, timeZone)
        val billionSecondsInstant = birthInstant.plusSeconds(BILLION_SECONDS)
        
        return billionSecondsInstant
    }

    fun isBillionSecondsReached(
        birthDate: LocalDate,
        birthTime: LocalTime
    ): Boolean {
        val billionSecondsInstant = calculateBillionSeconds(birthDate, birthTime)
        val now = getCurrentInstant()
        return now >= billionSecondsInstant
    }

    fun getSecondsUntilBillionSeconds(
        birthDate: LocalDate,
        birthTime: LocalTime
    ): Long {
        val billionSecondsInstant = calculateBillionSeconds(birthDate, birthTime)
        val now = getCurrentInstant()
        
        val birthMillis = billionSecondsInstant.toEpochMilliseconds()
        val nowMillis = now.toEpochMilliseconds()
        
        val diffMillis = birthMillis - nowMillis
        return diffMillis / 1000
    }
}