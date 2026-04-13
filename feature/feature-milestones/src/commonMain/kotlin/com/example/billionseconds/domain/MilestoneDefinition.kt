package com.example.billionseconds.domain

import kotlinx.datetime.Instant

data class MilestoneDefinition(
    val id: String,
    val secondsThreshold: Long,
    val title: String,
    val shortTitle: String,
    val isPrimary: Boolean,
    val isShareable: Boolean
)

data class UserMilestone(
    val definition: MilestoneDefinition,
    val targetInstant: Instant,
    val status: MilestoneStatus,
    val reachedAt: Instant? = null,
    val progressFromPrev: Float = 0f,
    val secondsRemaining: Long = 0L
)

sealed class MilestoneStatus {
    data class Reached(val reachedAt: Instant) : MilestoneStatus()
    data object Next     : MilestoneStatus()
    data object Upcoming : MilestoneStatus()
}
