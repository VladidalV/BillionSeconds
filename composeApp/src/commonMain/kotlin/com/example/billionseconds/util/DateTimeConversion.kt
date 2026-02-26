package com.example.billionseconds.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

expect fun localDateTimeToInstant(
    date: LocalDate,
    time: LocalTime,
    timeZone: TimeZone
): Instant