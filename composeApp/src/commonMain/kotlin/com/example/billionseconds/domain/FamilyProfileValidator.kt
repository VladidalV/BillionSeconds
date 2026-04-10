package com.example.billionseconds.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

object FamilyProfileValidator {

    private const val MAX_NAME_LENGTH = 40

    sealed class Error {
        data object NameEmpty    : Error()
        data object NameTooLong  : Error()
        data object DateRequired : Error()
        data object DateInFuture : Error()
        data object DateInvalid  : Error()
    }

    fun validateName(name: String): Error? = when {
        name.isBlank()              -> Error.NameEmpty
        name.length > MAX_NAME_LENGTH -> Error.NameTooLong
        else                        -> null
    }

    fun validateBirthDate(year: Int?, month: Int?, day: Int?, now: Instant): Error? {
        if (year == null || month == null || day == null) return Error.DateRequired

        try {
            LocalDate(year, month, day)
        } catch (e: IllegalArgumentException) {
            return Error.DateInvalid
        }

        val birthInstant = LocalDateTime(year, month, day, 0, 0, 0, 0)
            .toInstant(TimeZone.UTC)
        if (birthInstant >= now) return Error.DateInFuture

        return null
    }

    fun errorMessage(error: Error): String = when (error) {
        Error.NameEmpty    -> "Имя не может быть пустым"
        Error.NameTooLong  -> "Имя не должно превышать $MAX_NAME_LENGTH символов"
        Error.DateRequired -> "Выберите дату рождения"
        Error.DateInFuture -> "Дата рождения не может быть в будущем"
        Error.DateInvalid  -> "Некорректная дата"
    }
}
