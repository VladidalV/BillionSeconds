package com.example.billionseconds.domain

import com.example.billionseconds.data.model.BirthdayData
import kotlinx.datetime.*

object LifeStatsCalculator {

    const val HEART_RATE_BPM: Long       = 70L
    const val SLEEP_HOURS_PER_DAY: Double = 8.0
    const val LIFE_EXPECTANCY_YEARS: Double = 80.0

    data class RawStats(
        val secondsLived: Long,
        val minutesLived: Long,
        val hoursLived: Long,
        val daysLived: Long,
        val weeksLived: Long,
        val ageYears: Int,
        val ageMonths: Int,
        val ageDays: Int,
        val heartbeats: Long,
        val sleepDays: Long,
        val lifeProgressPct: Float,    // 0..100
        val progressToBillion: Float,  // 0..1
        val secondsRemaining: Long,
        val isApproximate: Boolean     // true если unknownTime
    )

    fun compute(
        birthData: BirthdayData,
        now: Instant,
        isUnknownTime: Boolean,
        progressFraction: Float,
        secondsRemaining: Long
    ): RawStats {
        val birthInstant = BillionSecondsCalculator.birthInstantFrom(birthData)
        val secondsLived = maxOf(0L, (now - birthInstant).inWholeSeconds)

        val minutesLived = secondsLived / 60L
        val hoursLived   = secondsLived / 3_600L
        val daysLived    = secondsLived / 86_400L
        val weeksLived   = secondsLived / 604_800L

        // Возраст через LocalDate для корректного учёта границ месяцев/лет
        val birthDate = LocalDate(birthData.year, birthData.month, birthData.day)
        val today     = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val ageYears  = birthDate.yearsUntil(today)
        val afterYears = birthDate.plus(ageYears, DateTimeUnit.YEAR)
        val ageMonths  = afterYears.monthsUntil(today)
        val afterMonths = afterYears.plus(ageMonths, DateTimeUnit.MONTH)
        val ageDays    = afterMonths.daysUntil(today)

        val heartbeats = minutesLived * HEART_RATE_BPM
        val sleepDays  = (daysLived * SLEEP_HOURS_PER_DAY / 24.0).toLong()

        val lifeExpectancyDays = LIFE_EXPECTANCY_YEARS * 365.25
        val lifeProgressPct    = (daysLived / lifeExpectancyDays * 100.0)
            .toFloat().coerceIn(0f, 100f)

        return RawStats(
            secondsLived    = secondsLived,
            minutesLived    = minutesLived,
            hoursLived      = hoursLived,
            daysLived       = daysLived,
            weeksLived      = weeksLived,
            ageYears        = ageYears,
            ageMonths       = ageMonths,
            ageDays         = ageDays,
            heartbeats      = heartbeats,
            sleepDays       = sleepDays,
            lifeProgressPct = lifeProgressPct,
            progressToBillion = progressFraction,
            secondsRemaining  = secondsRemaining,
            isApproximate   = isUnknownTime
        )
    }
}
