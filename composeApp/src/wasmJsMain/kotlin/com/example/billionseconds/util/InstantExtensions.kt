package com.example.billionseconds.util

import kotlinx.datetime.Instant

actual fun Instant.toEpochMilliseconds(): Long {
    return this.toEpochMilliseconds()
}

actual fun Instant.plusSeconds(seconds: Long): Instant {
    val secondsAsMillis = seconds * 1000
    val currentMillis = this.toEpochMilliseconds()
    val newMillis = currentMillis + secondsAsMillis
    return Instant.fromEpochMilliseconds(newMillis)
}