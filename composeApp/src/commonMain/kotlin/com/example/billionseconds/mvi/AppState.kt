package com.example.billionseconds.mvi

import com.example.billionseconds.domain.auth.AuthSource
import com.example.billionseconds.ui.auth.AuthUiState
import com.example.billionseconds.ui.event.EventUiState
import com.example.billionseconds.ui.timecapsule.TimeCapsuleUiState
import com.example.billionseconds.ui.countdown.CountdownUiState
import com.example.billionseconds.ui.lifestats.LifeStatsUiState
import com.example.billionseconds.ui.family.FamilyUiState
import com.example.billionseconds.ui.milestones.MilestonesUiState
import com.example.billionseconds.ui.onboarding.OnboardingUiState
import com.example.billionseconds.ui.profile.ProfileUiState
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
    val timeCapsule: TimeCapsuleUiState = TimeCapsuleUiState(),

    // Auth screen
    val auth: AuthUiState = AuthUiState(),
) {
    val onboarding: OnboardingUiState get() = OnboardingUiState(
        year = year,
        month = month,
        day = day,
        hour = hour,
        minute = minute,
        unknownTime = unknownTime,
        milestoneInstant = milestoneInstant,
        progressPercent = progressPercent,
        isMilestoneReached = isMilestoneReached,
        error = error
    )
}
