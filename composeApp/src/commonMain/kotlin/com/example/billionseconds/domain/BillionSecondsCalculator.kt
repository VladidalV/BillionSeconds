package com.example.billionseconds.domain

import com.example.billionseconds.data.model.BirthdayData
import kotlinx.datetime.*

object BillionSecondsCalculator {

    const val BILLION = 1_000_000_000L

    fun calculateMilestone(data: BirthdayData): Instant {
        val birthInstant = LocalDateTime(
            year = data.year,
            monthNumber = data.month,
            dayOfMonth = data.day,
            hour = data.hour,
            minute = data.minute,
            second = 0,
            nanosecond = 0
        ).toInstant(TimeZone.UTC)
        return birthInstant.plus(BILLION, DateTimeUnit.SECOND)
    }

    fun secondsUntil(milestone: Instant, now: Instant): Long =
        (milestone - now).inWholeSeconds

    fun isReached(milestone: Instant, now: Instant): Boolean =
        now >= milestone
}
