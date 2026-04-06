package com.example.billionseconds.mvi

import kotlinx.datetime.Instant

data class BirthdayState(
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val hour: Int = 0,
    val minute: Int = 0,
    val milestoneInstant: Instant? = null,
    val secondsRemaining: Long = 0L,
    val isMilestoneReached: Boolean = false,
    val showResult: Boolean = false,
    val error: String? = null
)
