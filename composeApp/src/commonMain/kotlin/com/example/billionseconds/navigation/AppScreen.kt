package com.example.billionseconds.navigation

sealed class AppScreen {
    data object OnboardingIntro  : AppScreen()
    data object OnboardingInput  : AppScreen()
    data object OnboardingResult : AppScreen()
    data class  Main(val tab: MainTab = MainTab.Home) : AppScreen()
}
