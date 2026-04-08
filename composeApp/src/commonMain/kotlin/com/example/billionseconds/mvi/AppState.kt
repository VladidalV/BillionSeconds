package com.example.billionseconds.mvi

import com.example.billionseconds.navigation.AppScreen
import kotlinx.datetime.Instant

data class AppState(
    // Navigation
    val screen: AppScreen = AppScreen.OnboardingIntro,

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

    val error: String? = null
)
