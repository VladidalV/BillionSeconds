package com.example.billionseconds.domain.model

import kotlinx.datetime.Instant

data class MilestoneResult(
    val milestoneInstant: Instant,
    val progressPercent: Float,
    val isMilestoneReached: Boolean,
    val secondsRemaining: Long
)
