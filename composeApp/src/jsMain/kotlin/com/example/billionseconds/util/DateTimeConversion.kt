package com.example.billionseconds.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

actual fun localDateTimeToInstant(
    date: LocalDate,
    time: LocalTime,
    timeZone: TimeZone
): Instant {
    val localDateTime = kotlinx.datetime.LocalDateTime(date, time)
    return localDateTime.toInstant(timeZone)
}