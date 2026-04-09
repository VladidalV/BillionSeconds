package com.example.billionseconds.mvi

import com.example.billionseconds.navigation.AppScreen

object AppReducer {

    fun reduce(state: AppState, intent: AppIntent): AppState = when (intent) {

        // Onboarding Screen 1
        is AppIntent.StartClicked ->
            state.copy(screen = AppScreen.OnboardingInput, error = null)

        // Onboarding Screen 2
        is AppIntent.OnboardingDateChanged ->
            state.copy(year = intent.year, month = intent.month, day = intent.day, error = null)

        is AppIntent.OnboardingTimeChanged ->
            state.copy(hour = intent.hour, minute = intent.minute)

        is AppIntent.UnknownTimeToggled -> {
            val newFlag = !state.unknownTime
            state.copy(
                unknownTime = newFlag,
                hour = if (newFlag) 12 else state.hour,
                minute = if (newFlag) 0 else state.minute
            )
        }

        // Side effects handled in AppStore; reducer only clears error
        is AppIntent.OnboardingCalculateClicked ->
            state.copy(error = null)

        // Side effect in AppStore
        is AppIntent.OnboardingContinueClicked ->
            state

        // Main app
        is AppIntent.DateChanged ->
            state.copy(year = intent.year, month = intent.month, day = intent.day, error = null)

        is AppIntent.TimeChanged ->
            state.copy(hour = intent.hour, minute = intent.minute)

        is AppIntent.CalculateClicked ->
            state.copy(error = null)

        is AppIntent.ClearClicked ->
            state.copy(
                year = null, month = null, day = null,
                hour = 12, minute = 0,
                milestoneInstant = null, secondsRemaining = 0L,
                progressPercent = 0f, isMilestoneReached = false,
                showMainResult = false, error = null,
                unknownTime = false
            )

        // Countdown screen — lifecycle (side effects handled in Store)
        is AppIntent.CountdownScreenStarted -> state
        is AppIntent.CountdownScreenResumed -> state

        // Countdown screen — action buttons (side effects only, no state change)
        is AppIntent.ShareClicked       -> state
        is AppIntent.CreateVideoClicked -> state
        is AppIntent.WriteLetterClicked -> state
        is AppIntent.AddFamilyClicked   -> state
        is AppIntent.LifeStatsClicked   ->
            state.copy(screen = com.example.billionseconds.navigation.AppScreen.LifeStats)

        // Navigation
        is AppIntent.BackClicked ->
            when (state.screen) {
                com.example.billionseconds.navigation.AppScreen.LifeStats ->
                    state.copy(screen = com.example.billionseconds.navigation.AppScreen.Main)
                else -> state
            }
    }
}
