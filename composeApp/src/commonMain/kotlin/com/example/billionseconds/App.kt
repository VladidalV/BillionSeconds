package com.example.billionseconds

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.billionseconds.data.AppSettingsRepository
import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.FamilyProfileRepository
import com.example.billionseconds.data.TimeCapsuleRepository
import com.example.billionseconds.data.createAppSettingsStorage
import com.example.billionseconds.data.createBirthdayStorage
import com.example.billionseconds.data.createFamilyProfileStorage
import com.example.billionseconds.data.createTimeCapsuleStorage
import com.example.billionseconds.data.event.EventHistoryRepository
import com.example.billionseconds.data.event.createEventHistoryStorage
import com.example.billionseconds.network.createNetworkComponents
import com.example.billionseconds.ui.auth.AuthEntryScreen
import com.example.billionseconds.domain.event.model.EventSource
import com.example.billionseconds.mvi.AppEffect
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.AppStore
import com.example.billionseconds.mvi.authAdapter
import com.example.billionseconds.mvi.eventAdapter
import com.example.billionseconds.mvi.onboardingAdapter
import com.example.billionseconds.mvi.timeCapsuleAdapter
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
        val birthdayRepo     = BirthdayRepository(createBirthdayStorage())
        val familyRepo       = FamilyProfileRepository(createFamilyProfileStorage())
        val settingsRepo     = AppSettingsRepository(createAppSettingsStorage())
        val eventHistoryRepo = EventHistoryRepository(createEventHistoryStorage())
        val timeCapsuleRepo  = TimeCapsuleRepository(createTimeCapsuleStorage())
        val network = createNetworkComponents(
            familyRepository       = familyRepo,
            settingsRepository     = settingsRepo,
            eventHistoryRepository = eventHistoryRepo,
            timeCapsuleRepository  = timeCapsuleRepo,
            birthdayRepository     = birthdayRepo,
        )
        AppStore(
            repository             = birthdayRepo,
            familyRepository       = familyRepo,
            settingsRepository     = settingsRepo,
            eventHistoryRepository = eventHistoryRepo,
            syncManager            = network.syncManager,
            authManager            = network.authManager,
        )
    }
    DisposableEffect(store) {
        onDispose { store.dispose() }
    }

    val state by store.state.collectAsState()
    val currentScreen by store.navigator.current.collectAsState()

    var comingSoonFeature by remember { mutableStateOf<String?>(null) }
    var celebrationMilestoneId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

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
                is AppEffect.ShareEventPayload -> shareText(effect.payload.text)
                is AppEffect.ShowEventError -> Unit // TODO: snackbar
                // Auth
                is AppEffect.LaunchGoogleSignIn      -> onLaunchGoogleSignIn(store::dispatch)
                is AppEffect.LaunchAppleSignIn       -> onLaunchAppleSignIn(store::dispatch)
                is AppEffect.AuthSuccess             -> snackbarHostState.showSnackbar("Добро пожаловать!")
                is AppEffect.DismissAuthScreen       -> Unit // навигация уже выполнена в AppStore
                is AppEffect.ShowLogoutConfirmDialog -> Unit // handled in ProfileRootContent via confirmDialog state
                is AppEffect.SessionExpiredBanner    -> snackbarHostState.showSnackbar("Сессия истекла. Войдите снова.")
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
        Box(modifier = Modifier.fillMaxSize()) {
            when (val screen = currentScreen) {
                AppScreen.OnboardingIntro ->
                    OnboardingIntroScreen(onAction = onboardingAdapter(store::dispatch))

                AppScreen.OnboardingInput ->
                    OnboardingInputScreen(uiState = state.onboarding, onAction = onboardingAdapter(store::dispatch))

                AppScreen.OnboardingResult ->
                    OnboardingResultScreen(uiState = state.onboarding, onAction = onboardingAdapter(store::dispatch))

                is AppScreen.Main ->
                    MainScaffold(state = state, selectedTab = screen.tab, onIntent = store::dispatch)

                is AppScreen.EventScreen ->
                    EventScreen(
                        uiState  = state.event,
                        onAction = eventAdapter(store::dispatch)
                    )

                AppScreen.TimeCapsule ->
                    TimeCapsuleScreen(
                        uiState  = state.timeCapsule,
                        onAction = timeCapsuleAdapter(store::dispatch)
                    )

                is AppScreen.AuthEntry ->
                    AuthEntryScreen(
                        uiState  = state.auth,
                        onAction = authAdapter(store::dispatch),
                    )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = androidx.compose.ui.graphics.Color(0xFF2C2C3A),
                        contentColor = androidx.compose.ui.graphics.Color.White,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            )
        }
    }
}

expect fun shareText(text: String)
expect fun openUrl(url: String)

/** Платформенный код запускает Google Sign-In и диспатчит GoogleTokenReceived или SignInFailed. */
expect fun onLaunchGoogleSignIn(dispatch: (com.example.billionseconds.mvi.AppIntent) -> Unit)

/** Платформенный код запускает Apple Sign-In и диспатчит AppleTokenReceived или SignInFailed. */
expect fun onLaunchAppleSignIn(dispatch: (com.example.billionseconds.mvi.AppIntent) -> Unit)
