package com.example.billionseconds

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.billionseconds.data.AppSettingsRepository
import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.FamilyProfileRepository
import com.example.billionseconds.data.createAppSettingsStorage
import com.example.billionseconds.data.createBirthdayStorage
import com.example.billionseconds.data.createFamilyProfileStorage
import com.example.billionseconds.mvi.AppEffect
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.AppStore
import com.example.billionseconds.navigation.AppScreen
import com.example.billionseconds.navigation.MainTab
import com.example.billionseconds.ui.main.MainScaffold
import com.example.billionseconds.ui.onboarding.OnboardingInputScreen
import com.example.billionseconds.ui.onboarding.OnboardingIntroScreen
import com.example.billionseconds.ui.onboarding.OnboardingResultScreen
import com.example.billionseconds.ui.shared.ComingSoonSheet

@Composable
fun App() {
    val store = remember {
        AppStore(
            repository        = BirthdayRepository(createBirthdayStorage()),
            familyRepository  = FamilyProfileRepository(createFamilyProfileStorage()),
            settingsRepository = AppSettingsRepository(createAppSettingsStorage())
        )
    }
    DisposableEffect(store) {
        onDispose { store.dispose() }
    }

    val state by store.state.collectAsState()

    var comingSoonFeature by remember { mutableStateOf<String?>(null) }
    var celebrationMilestoneId by remember { mutableStateOf<String?>(null) }

    // Обработка одноразовых эффектов
    LaunchedEffect(store) {
        store.effect.collect { effect ->
            when (effect) {
                is AppEffect.ExitApp                  -> exitApp()
                is AppEffect.ShareText                -> shareText(effect.text)
                is AppEffect.ShowComingSoon           -> comingSoonFeature = effect.feature
                is AppEffect.ShowError                -> Unit // TODO: snackbar
                is AppEffect.ShowMilestoneCelebration -> celebrationMilestoneId = effect.milestoneId
                is AppEffect.ShareMilestone           -> shareText(effect.text)
                is AppEffect.ActiveProfileChanged     -> Unit // TODO: snackbar "Переключено на профиль"
                is AppEffect.ShowFamilyError          -> Unit // TODO: snackbar
                is AppEffect.NavigateToFamily         ->
                    store.dispatch(AppIntent.TabSelected(MainTab.Family))
                is AppEffect.LaunchExternalUrl        -> openUrl(effect.url)
                is AppEffect.ShowProfileError         -> Unit // TODO: snackbar
                is AppEffect.OnboardingReset          -> Unit // state уже AppState(); экран переключится сам
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

            is AppScreen.Main ->
                MainScaffold(state = state, onIntent = store::dispatch)
        }
    }
}

expect fun shareText(text: String)
expect fun openUrl(url: String)
