package com.example.billionseconds.domain

import com.example.billionseconds.data.model.BirthdayData
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BillionSecondsCalculatorTest {

    private val birthday = BirthdayData(year = 1990, month = 6, day = 15, hour = 12, minute = 0)

    @Test
    fun milestoneIsExactlyBillionSecondsAfterBirth() {
        val milestone = BillionSecondsCalculator.calculateMilestone(birthday)
        val birthInstant = LocalDateTime(1990, 6, 15, 12, 0, 0, 0).toInstant(TimeZone.UTC)
        val expected = birthInstant.plus(1_000_000_000L, DateTimeUnit.SECOND)
        assertEquals(expected, milestone)
    }

    @Test
    fun isReachedReturnsTrueWhenNowIsAfterMilestone() {
        val milestone = BillionSecondsCalculator.calculateMilestone(birthday)
        val afterMilestone = milestone.plus(1, DateTimeUnit.SECOND)
        assertTrue(BillionSecondsCalculator.isReached(milestone, afterMilestone))
    }

    @Test
    fun isReachedReturnsFalseWhenNowIsBeforeMilestone() {
        val milestone = BillionSecondsCalculator.calculateMilestone(birthday)
        val beforeMilestone = milestone.minus(1, DateTimeUnit.SECOND)
        assertFalse(BillionSecondsCalculator.isReached(milestone, beforeMilestone))
    }

    @Test
    fun isReachedReturnsTrueExactlyAtMilestone() {
        val milestone = BillionSecondsCalculator.calculateMilestone(birthday)
        assertTrue(BillionSecondsCalculator.isReached(milestone, milestone))
    }

    @Test
    fun secondsUntilReturnsCorrectValue() {
        val milestone = BillionSecondsCalculator.calculateMilestone(birthday)
        val now = milestone.minus(500L, DateTimeUnit.SECOND)
        assertEquals(500L, BillionSecondsCalculator.secondsUntil(milestone, now))
    }

    @Test
    fun secondsUntilReturnsNegativeWhenPast() {
        val milestone = BillionSecondsCalculator.calculateMilestone(birthday)
        val now = milestone.plus(100L, DateTimeUnit.SECOND)
        assertEquals(-100L, BillionSecondsCalculator.secondsUntil(milestone, now))
    }
}
