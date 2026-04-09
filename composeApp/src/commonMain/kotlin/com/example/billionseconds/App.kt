package com.example.billionseconds

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.createBirthdayStorage
import com.example.billionseconds.mvi.AppEffect
import com.example.billionseconds.mvi.AppStore
import com.example.billionseconds.navigation.AppScreen
import com.example.billionseconds.ui.BirthdayScreen
import com.example.billionseconds.ui.countdown.CountdownScreen
import com.example.billionseconds.ui.lifestats.LifeStatsScreen
import com.example.billionseconds.ui.onboarding.OnboardingInputScreen
import com.example.billionseconds.ui.onboarding.OnboardingIntroScreen
import com.example.billionseconds.ui.onboarding.OnboardingResultScreen
import com.example.billionseconds.ui.shared.ComingSoonSheet

@Composable
fun App() {
    val store = remember {
        AppStore(BirthdayRepository(createBirthdayStorage()))
    }
    DisposableEffect(store) {
        onDispose { store.dispose() }
    }

    val state by store.state.collectAsState()

    // ComingSoon sheet state — управляется через эффекты
    var comingSoonFeature by remember { mutableStateOf<String?>(null) }

    // Обработка одноразовых эффектов
    LaunchedEffect(store) {
        store.effect.collect { effect ->
            when (effect) {
                is AppEffect.NavigateToLifeStats -> Unit // уже обработано через state
                is AppEffect.ShareText           -> shareText(effect.text)
                is AppEffect.ShowComingSoon      -> comingSoonFeature = effect.feature
                is AppEffect.ShowError           -> Unit // TODO: snackbar
            }
        }
    }

    comingSoonFeature?.let { feature ->
        ComingSoonSheet(
            feature = feature,
            onDismiss = { comingSoonFeature = null }
        )
    }

    MaterialTheme {
        when (state.screen) {
            AppScreen.OnboardingIntro ->
                OnboardingIntroScreen(onIntent = store::dispatch)

            AppScreen.OnboardingInput ->
                OnboardingInputScreen(state = state, onIntent = store::dispatch)

            AppScreen.OnboardingResult ->
                OnboardingResultScreen(state = state, onIntent = store::dispatch)

            AppScreen.Main ->
                CountdownScreen(state = state, onIntent = store::dispatch)

            AppScreen.LifeStats ->
                LifeStatsScreen(countdown = state.countdown, onIntent = store::dispatch)
        }
    }
}

expect fun shareText(text: String)
