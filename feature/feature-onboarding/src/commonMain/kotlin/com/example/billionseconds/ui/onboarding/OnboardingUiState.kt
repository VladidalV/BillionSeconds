package com.example.billionseconds.ui.onboarding

import kotlinx.datetime.Instant

data class OnboardingUiState(
    val year: Int?           = null,
    val month: Int?          = null,
    val day: Int?            = null,
    val hour: Int            = 12,
    val minute: Int          = 0,
    val unknownTime: Boolean = false,
    val milestoneInstant: Instant?  = null,
    val progressPercent: Float      = 0f,
    val isMilestoneReached: Boolean = false,
    val error: String?       = null
)
