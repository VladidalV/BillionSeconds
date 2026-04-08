package com.example.billionseconds.mvi

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

    // Main app
    data class DateChanged(val year: Int, val month: Int, val day: Int) : AppIntent()
    data class TimeChanged(val hour: Int, val minute: Int) : AppIntent()
    data object CalculateClicked : AppIntent()
    data object ClearClicked : AppIntent()
}
