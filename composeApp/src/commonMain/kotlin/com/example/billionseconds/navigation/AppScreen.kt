package com.example.billionseconds.navigation

import com.example.billionseconds.domain.event.model.EventSource

sealed class AppScreen {
    data object OnboardingIntro  : AppScreen()
    data object OnboardingInput  : AppScreen()
    data object OnboardingResult : AppScreen()
    data class  Main(val tab: MainTab = MainTab.Home) : AppScreen()
    data class  EventScreen(
        val profileId: String,
        val source: EventSource = EventSource.MANUAL
    ) : AppScreen()
}
