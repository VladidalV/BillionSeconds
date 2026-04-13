package com.example.billionseconds.mvi

import com.example.billionseconds.mvi.event.EventUiState
import kotlinx.datetime.Instant

data class AppState(
    // Onboarding input
    val year: Int?           = null,
    val month: Int?          = null,
    val day: Int?            = null,
    val hour: Int            = 12,
    val minute: Int          = 0,
    val unknownTime: Boolean = false,

    // Onboarding result
    val milestoneInstant: Instant?  = null,
    val progressPercent: Float      = 0f,
    val isMilestoneReached: Boolean = false,

    // Main app
    val secondsRemaining: Long  = 0L,
    val showMainResult: Boolean = false,

    val error: String? = null,

    // Countdown screen
    val countdown: CountdownUiState = CountdownUiState(),

    // Life Stats screen
    val lifeStats: LifeStatsUiState = LifeStatsUiState(),

    // Milestones screen
    val milestones: MilestonesUiState = MilestonesUiState(),

    // Family screen
    val family: FamilyUiState = FamilyUiState(),

    // Profile screen
    val profile: ProfileUiState = ProfileUiState(),

    // Event Screen
    val event: EventUiState = EventUiState(),

    // Time Capsule screen
    val timeCapsule: TimeCapsuleUiState = TimeCapsuleUiState()
)
