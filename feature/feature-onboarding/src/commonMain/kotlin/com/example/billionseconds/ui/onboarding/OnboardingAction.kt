package com.example.billionseconds.ui.onboarding

sealed class OnboardingAction {

    data object StartClicked : OnboardingAction()

    data class DateChanged(val year: Int, val month: Int, val day: Int) : OnboardingAction()
    data class TimeChanged(val hour: Int, val minute: Int) : OnboardingAction()
    data object UnknownTimeToggled : OnboardingAction()
    data object CalculateClicked : OnboardingAction()

    data object ContinueClicked : OnboardingAction()
}
