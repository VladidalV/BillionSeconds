package com.example.billionseconds.domain

import kotlinx.datetime.*

object BirthdayValidator {

    sealed class ValidationError {
        data object DateRequired   : ValidationError()
        data object DateInFuture   : ValidationError()
        data object DateInvalidDay : ValidationError()
        data object YearOutOfRange : ValidationError()
    }

    fun validate(year: Int?, month: Int?, day: Int?, now: Instant): ValidationError? {
        if (year == null || month == null || day == null) return ValidationError.DateRequired
        if (year < 1900) return ValidationError.YearOutOfRange

        val localDate = try {
            LocalDate(year, month, day)
        } catch (e: IllegalArgumentException) {
            return ValidationError.DateInvalidDay
        }

        val birthInstant = LocalDateTime(year, month, day, 0, 0, 0, 0)
            .toInstant(TimeZone.UTC)
        if (birthInstant >= now) return ValidationError.DateInFuture

        return null
    }
}
