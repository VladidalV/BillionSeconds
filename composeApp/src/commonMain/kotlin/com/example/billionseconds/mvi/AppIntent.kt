package com.example.billionseconds.mvi

import com.example.billionseconds.navigation.MainTab

sealed class AppIntent {

    // Onboarding Screen 1
    data object StartClicked : AppIntent()

    // Onboarding Screen 2
    data class OnboardingDateChanged(val year: Int, val month: Int, val day: Int) : AppIntent()
    data class OnboardingTimeChanged(val hour: Int, val minute: Int) : AppIntent()
    data object UnknownTimeToggled : AppIntent()
    data object OnboardingCalculateClicked : AppIntent()

    // Onboarding Screen 3
    data object OnboardingContinueClicked : AppIntent()

    // Main app (legacy BirthdayScreen — may be removed later)
    data class DateChanged(val year: Int, val month: Int, val day: Int) : AppIntent()
    data class TimeChanged(val hour: Int, val minute: Int) : AppIntent()
    data object CalculateClicked : AppIntent()
    data object ClearClicked : AppIntent()

    // Bottom navigation
    data class TabSelected(val tab: MainTab) : AppIntent()

    // Countdown screen — lifecycle
    data object CountdownScreenStarted : AppIntent()
    data object CountdownScreenResumed : AppIntent()

    // Countdown screen — action buttons
    data object ShareClicked       : AppIntent()
    data object CreateVideoClicked : AppIntent()
    data object WriteLetterClicked : AppIntent()
    data object AddFamilyClicked   : AppIntent()

    // Life Stats screen — lifecycle
    data object LifeStatsScreenStarted : AppIntent()
    data object LifeStatsScreenResumed : AppIntent()

    // Navigation
    data object BackClicked : AppIntent()
}
