package com.example.billionseconds.util

import kotlinx.datetime.Instant
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
actual fun currentInstant(): Instant = Clock.System.now()
