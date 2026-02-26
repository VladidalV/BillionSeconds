package com.example.billionseconds.util

import kotlinx.datetime.Instant

expect fun Instant.toEpochMilliseconds(): Long

expect fun Instant.plusSeconds(seconds: Long): Instant