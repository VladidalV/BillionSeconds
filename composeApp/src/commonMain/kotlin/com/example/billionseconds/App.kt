package com.example.billionseconds

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.createBirthdayStorage
import com.example.billionseconds.mvi.AppStore
import com.example.billionseconds.navigation.AppScreen
import com.example.billionseconds.ui.BirthdayScreen
import com.example.billionseconds.ui.ResultScreen
import com.example.billionseconds.ui.onboarding.OnboardingIntroScreen
import com.example.billionseconds.ui.onboarding.OnboardingInputScreen
import com.example.billionseconds.ui.onboarding.OnboardingResultScreen

@Composable
fun App() {
    val store = remember {
        AppStore(BirthdayRepository(createBirthdayStorage()))
    }
    DisposableEffect(store) {
        onDispose { store.dispose() }
    }

    val state by store.state.collectAsState()

    MaterialTheme {
        when (state.screen) {
            AppScreen.OnboardingIntro ->
                OnboardingIntroScreen(onIntent = store::dispatch)

            AppScreen.OnboardingInput ->
                OnboardingInputScreen(state = state, onIntent = store::dispatch)

            AppScreen.OnboardingResult ->
                OnboardingResultScreen(state = state, onIntent = store::dispatch)

            AppScreen.Main ->
                if (state.showMainResult) {
                    ResultScreen(state = state, onIntent = store::dispatch)
                } else {
                    BirthdayScreen(state = state, onIntent = store::dispatch)
                }
        }
    }
}
