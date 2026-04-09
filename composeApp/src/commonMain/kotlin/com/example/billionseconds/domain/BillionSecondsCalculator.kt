package com.example.billionseconds.domain

import com.example.billionseconds.data.model.BirthdayData
import com.example.billionseconds.domain.model.MilestoneResult
import kotlinx.datetime.*

object BillionSecondsCalculator {

    const val BILLION = 1_000_000_000L

    internal fun birthInstantFrom(data: BirthdayData): Instant =
        LocalDateTime(
            year = data.year,
            monthNumber = data.month,
            dayOfMonth = data.day,
            hour = data.hour,
            minute = data.minute,
            second = 0,
            nanosecond = 0
        ).toInstant(TimeZone.UTC)

    fun calculateMilestone(data: BirthdayData): Instant =
        birthInstantFrom(data).plus(BILLION, DateTimeUnit.SECOND)

    fun calculateProgress(birthInstant: Instant, now: Instant): Float {
        val elapsed = (now - birthInstant).inWholeSeconds.toFloat()
        return (elapsed / BILLION).coerceIn(0f, 1f)
    }

    fun computeAll(data: BirthdayData, now: Instant): MilestoneResult {
        val birth = birthInstantFrom(data)
        val milestone = birth.plus(BILLION, DateTimeUnit.SECOND)
        return MilestoneResult(
            milestoneInstant = milestone,
            progressPercent = calculateProgress(birth, now),
            isMilestoneReached = now >= milestone,
            secondsRemaining = maxOf(0L, (milestone - now).inWholeSeconds)
        )
    }

    fun secondsUntil(milestone: Instant, now: Instant): Long =
        (milestone - now).inWholeSeconds

    fun isReached(milestone: Instant, now: Instant): Boolean =
        now >= milestone
}
