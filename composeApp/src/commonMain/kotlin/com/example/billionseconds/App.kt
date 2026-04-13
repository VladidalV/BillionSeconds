package com.example.billionseconds

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.billionseconds.data.AppSettingsRepository
import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.FamilyProfileRepository
import com.example.billionseconds.data.createAppSettingsStorage
import com.example.billionseconds.data.createBirthdayStorage
import com.example.billionseconds.data.createFamilyProfileStorage
import com.example.billionseconds.data.event.EventHistoryRepository
import com.example.billionseconds.data.event.createEventHistoryStorage
import com.example.billionseconds.domain.event.model.EventSource
import com.example.billionseconds.mvi.AppEffect
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.AppStore
import com.example.billionseconds.navigation.AppScreen
import com.example.billionseconds.navigation.MainTab
import com.example.billionseconds.ui.event.EventScreen
import com.example.billionseconds.ui.main.MainScaffold
import com.example.billionseconds.ui.timecapsule.TimeCapsuleScreen
import com.example.billionseconds.ui.onboarding.OnboardingInputScreen
import com.example.billionseconds.ui.onboarding.OnboardingIntroScreen
import com.example.billionseconds.ui.onboarding.OnboardingResultScreen
import com.example.billionseconds.ui.shared.ComingSoonSheet

@Composable
fun App() {
    val store = remember {
        AppStore(
            repository             = BirthdayRepository(createBirthdayStorage()),
            familyRepository       = FamilyProfileRepository(createFamilyProfileStorage()),
            settingsRepository     = AppSettingsRepository(createAppSettingsStorage()),
            eventHistoryRepository = EventHistoryRepository(createEventHistoryStorage())
        )
    }
    DisposableEffect(store) {
        onDispose { store.dispose() }
    }

    val state by store.state.collectAsState()
    val currentScreen by store.navigator.current.collectAsState()

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
                // Event Screen effects
                is AppEffect.NavigateToEventScreen ->
                    store.dispatch(AppIntent.Event.ScreenOpened(effect.profileId, effect.source))
                is AppEffect.NavigateToShareFromEvent ->
                    store.dispatch(AppIntent.Event.ShareClicked)
                is AppEffect.NavigateToMilestonesFromEvent ->
                    store.dispatch(AppIntent.TabSelected(MainTab.Milestones))
                is AppEffect.NavigateToStatsFromEvent ->
                    store.dispatch(AppIntent.TabSelected(MainTab.Stats))
                is AppEffect.NavigateToHomeFromEvent ->
                    store.dispatch(AppIntent.TabSelected(MainTab.Home))
                is AppEffect.CloseEventScreen ->
                    store.dispatch(AppIntent.Event.BackPressed)
                is AppEffect.TriggerCelebrationAnimation -> Unit // обрабатывается в EventScreen
                is AppEffect.ShareEventPayload -> shareText(effect.payload.text)
                is AppEffect.ShowEventError -> Unit // TODO: snackbar
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
        when (val screen = currentScreen) {
            AppScreen.OnboardingIntro ->
                OnboardingIntroScreen(onIntent = store::dispatch)

            AppScreen.OnboardingInput ->
                OnboardingInputScreen(state = state, onIntent = store::dispatch)

            AppScreen.OnboardingResult ->
                OnboardingResultScreen(state = state, onIntent = store::dispatch)

            is AppScreen.Main ->
                MainScaffold(state = state, selectedTab = screen.tab, onIntent = store::dispatch)

            is AppScreen.EventScreen ->
                EventScreen(
                    state    = state.event,
                    effects  = store.effect,
                    onIntent = store::dispatch
                )

            AppScreen.TimeCapsule ->
                TimeCapsuleScreen(
                    uiState  = state.timeCapsule,
                    onIntent = store::dispatch
                )
        }
    }
}

expect fun shareText(text: String)
expect fun openUrl(url: String)
