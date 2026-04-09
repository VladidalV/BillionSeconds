package com.example.billionseconds.navigation

sealed class AppScreen {
    data object OnboardingIntro  : AppScreen()
    data object OnboardingInput  : AppScreen()
    data object OnboardingResult : AppScreen()
    data object Main             : AppScreen()
    data object LifeStats        : AppScreen()
}
