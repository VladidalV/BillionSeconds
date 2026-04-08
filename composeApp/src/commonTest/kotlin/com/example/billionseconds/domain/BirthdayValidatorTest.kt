package com.example.billionseconds.domain

import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

class BirthdayValidatorTest {

    private val now = LocalDateTime(2024, 1, 1, 12, 0, 0, 0).toInstant(TimeZone.UTC)

    @Test
    fun returnsNullForValidPastDate() {
        assertNull(BirthdayValidator.validate(1990, 6, 15, now))
    }

    @Test
    fun returnsDateRequiredWhenYearIsNull() {
        assertIs<BirthdayValidator.ValidationError.DateRequired>(
            BirthdayValidator.validate(null, 6, 15, now)
        )
    }

    @Test
    fun returnsDateRequiredWhenMonthIsNull() {
        assertIs<BirthdayValidator.ValidationError.DateRequired>(
            BirthdayValidator.validate(1990, null, 15, now)
        )
    }

    @Test
    fun returnsDateRequiredWhenDayIsNull() {
        assertIs<BirthdayValidator.ValidationError.DateRequired>(
            BirthdayValidator.validate(1990, 6, null, now)
        )
    }

    @Test
    fun returnsYearOutOfRangeForYearBefore1900() {
        assertIs<BirthdayValidator.ValidationError.YearOutOfRange>(
            BirthdayValidator.validate(1899, 1, 1, now)
        )
    }

    @Test
    fun returnsDateInvalidDayForInvalidCalendarDate() {
        assertIs<BirthdayValidator.ValidationError.DateInvalidDay>(
            BirthdayValidator.validate(1990, 2, 30, now)
        )
    }

    @Test
    fun returnsDateInFutureForFutureDate() {
        assertIs<BirthdayValidator.ValidationError.DateInFuture>(
            BirthdayValidator.validate(2099, 1, 1, now)
        )
    }

    @Test
    fun returnsNullForBirthBefore1968MilestoneAlreadyPassed() {
        // Рождённый до 1968 → milestone уже прошёл, но дата валидна
        assertNull(BirthdayValidator.validate(1960, 1, 1, now))
    }
}
