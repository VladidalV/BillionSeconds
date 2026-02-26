package com.example.billionseconds.util

actual fun getCurrentInstant(): kotlinx.datetime.Instant {
    return kotlinx.datetime.Clock.System.now()
}