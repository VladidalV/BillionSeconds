package com.example.billionseconds.util

import kotlinx.datetime.Clock

actual fun getCurrentInstant(): kotlinx.datetime.Instant {
    return Clock.System.now()
}