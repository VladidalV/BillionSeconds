package com.example.billionseconds.mvi

import com.example.billionseconds.data.AppSettingsRepository
import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.FamilyProfileRepository
import com.example.billionseconds.data.TimeCapsuleRepository
import com.example.billionseconds.data.createTimeCapsuleStorage
import com.example.billionseconds.data.event.EventHistoryRepository
import com.example.billionseconds.domain.auth.AuthSource
import com.example.billionseconds.network.auth.AuthManager
import com.example.billionseconds.ui.auth.AuthUiState
import com.example.billionseconds.domain.CapsuleListGrouper
import com.example.billionseconds.domain.CapsuleStatus
import com.example.billionseconds.domain.CapsuleUiMapper
import com.example.billionseconds.domain.CapsuleUnlockResolver
import com.example.billionseconds.domain.TimeCapsuleValidator
import com.example.billionseconds.domain.model.TimeCapsule
import com.example.billionseconds.domain.model.UnlockCondition
import com.example.billionseconds.data.model.BirthdayData
import com.example.billionseconds.data.model.FamilyProfile
import com.example.billionseconds.data.model.RelationType
import com.example.billionseconds.data.model.toBirthdayData
import com.example.billionseconds.domain.BillionSecondsCalculator
import com.example.billionseconds.domain.BirthdayValidator
import com.example.billionseconds.domain.CountdownFormatter
import com.example.billionseconds.domain.FamilyProfileCalculator
import com.example.billionseconds.domain.FamilyProfileFormatter
import com.example.billionseconds.domain.FamilyProfileValidator
import com.example.billionseconds.domain.LifeStatsCalculator
import com.example.billionseconds.domain.LifeStatsFormatter
import com.example.billionseconds.domain.MilestoneConfig
import com.example.billionseconds.domain.MilestoneStatus
import com.example.billionseconds.domain.MilestonesCalculator
import com.example.billionseconds.domain.MilestonesFormatter
import com.example.billionseconds.domain.event.EventEligibilityChecker
import com.example.billionseconds.domain.event.EventHistoryManager
import com.example.billionseconds.domain.event.EventScreenDataBuilder
import com.example.billionseconds.domain.event.EventSharePayloadBuilder
import com.example.billionseconds.domain.event.EventUiMapper
import com.example.billionseconds.domain.event.model.EventDomainModel
import com.example.billionseconds.domain.event.model.EventEligibilityStatus
import com.example.billionseconds.domain.event.model.EventMode
import com.example.billionseconds.domain.event.model.EventSource
import com.example.billionseconds.domain.model.MilestoneResult
import com.example.billionseconds.domain.model.toEventStatus
import com.example.billionseconds.ui.event.EventScreenStatus
import com.example.billionseconds.ui.timecapsule.CapsuleFormDraft
import com.example.billionseconds.ui.timecapsule.ConditionType
import com.example.billionseconds.ui.timecapsule.TimeCapsuleError
import com.example.billionseconds.ui.timecapsule.TimeCapsuleSubScreen
import com.example.billionseconds.ui.timecapsule.TimeCapsuleUiState
import com.example.billionseconds.ui.countdown.CountdownError
import com.example.billionseconds.ui.countdown.CountdownUiState
import com.example.billionseconds.ui.lifestats.LifeStatsError
import com.example.billionseconds.ui.lifestats.LifeStatsUiState
import com.example.billionseconds.ui.lifestats.StatItem
import com.example.billionseconds.ui.family.FamilyError
import com.example.billionseconds.ui.family.FamilyProfileUiItem
import com.example.billionseconds.ui.family.FamilySubScreen
import com.example.billionseconds.ui.family.FamilyUiState
import com.example.billionseconds.ui.family.ProfileFormDraft
import com.example.billionseconds.ui.milestones.MilestoneUiItem
import com.example.billionseconds.ui.milestones.MilestonesError
import com.example.billionseconds.ui.milestones.MilestonesUiState
import com.example.billionseconds.ui.profile.ActiveProfileSummary
import com.example.billionseconds.ui.profile.LegalLinkType
import com.example.billionseconds.ui.profile.ProfileConfirmDialog
import com.example.billionseconds.ui.profile.ProfileSubScreen
import com.example.billionseconds.ui.profile.ProfileUiState
import com.example.billionseconds.navigation.AppScreen
import com.example.billionseconds.navigation.MainTab
import com.example.billionseconds.navigation.command.NavCommand
import com.example.billionseconds.navigation.navigator.AppNavigator
import com.example.billionseconds.util.AppMetaProvider
import com.example.billionseconds.util.currentInstant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime

private const val MAX_PROFILES = 5
private const val PRIMARY_PROFILE_ID = "self_primary"

private const val PRIVACY_POLICY_URL = "https://example.com/privacy"
private const val TERMS_URL = "https://example.com/terms"

class AppStore(
    private val repository: BirthdayRepository,
    private val familyRepository: FamilyProfileRepository,
    private val settingsRepository: AppSettingsRepository,
    private val eventHistoryRepository: EventHistoryRepository,
    private val syncManager: com.example.billionseconds.network.sync.SyncManager? = null,
    private val authManager: AuthManager? = null,
) {
    private val eventEligibilityChecker = EventEligibilityChecker(eventHistoryRepository)
    private val eventHistoryManager = EventHistoryManager(eventHistoryRepository)
    private val eventDataBuilder = EventScreenDataBuilder(eventEligibilityChecker, eventHistoryManager)

    private val timeCapsuleRepository = TimeCapsuleRepository(createTimeCapsuleStorage())

    val navigator: AppNavigator

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AppEffect>(extraBufferCapacity = 16)
    val effect: SharedFlow<AppEffect> = _effect.asSharedFlow()

    private var tickJob: Job? = null

    init {
        val onboardingDone = repository.isOnboardingCompleted()
        val saved = repository.getBirthday()

        // Миграция существующих пользователей: данные есть, флаг не установлен
        if (!onboardingDone && saved != null) {
            repository.setOnboardingCompleted(true)
        }

        // Миграция legacy birthday данных в FamilyProfile
        migrateLegacyBirthdayToFamily()

        var initialScreen: AppScreen = AppScreen.OnboardingIntro

        if (repository.isOnboardingCompleted()) {
            val activeProfile = getActiveProfileOrFallback()
            if (activeProfile != null) {
                val now = currentInstant()
                val birthData = activeProfile.toBirthdayData()
                val result = BillionSecondsCalculator.computeAll(birthData, now)
                val unknownTime = activeProfile.unknownBirthTime
                _state.value = AppState(
                    year = activeProfile.birthYear,
                    month = activeProfile.birthMonth,
                    day = activeProfile.birthDay,
                    hour = activeProfile.birthHour,
                    minute = activeProfile.birthMinute,
                    unknownTime = unknownTime,
                    milestoneInstant = result.milestoneInstant,
                    progressPercent = result.progressPercent,
                    isMilestoneReached = result.isMilestoneReached,
                    secondsRemaining = result.secondsRemaining,
                    showMainResult = true,
                    countdown  = buildCountdownUiState(result, unknownTime, now, activeProfile.name),
                    lifeStats  = buildLifeStatsUiState(birthData, result, unknownTime, now),
                    milestones = buildMilestonesUiState(birthData, unknownTime, now),
                    family     = buildFamilyUiState(now),
                    profile    = buildProfileUiState(now)
                )
                initialScreen = AppScreen.Main(MainTab.Home)
                startTick(result.milestoneInstant)
                // Проверка event eligibility при старте приложения
                checkEventEligibilityAndTrigger(activeProfile, now)
            }
        }

        navigator = AppNavigator(initialScreen)

        // Background sync — does not block UI initialisation
        if (syncManager != null) {
            scope.launch(Dispatchers.Default) {
                syncManager.syncOnStart()
                // After sync completes, refresh local state on main thread
                dispatch(AppIntent.SyncCompleted)
            }
        }

        // Observe auth state changes to react to session expiry
        if (authManager != null) {
            scope.launch {
                authManager.authState.collect { state ->
                    if (state is com.example.billionseconds.domain.auth.AuthState.Error &&
                        state.type is com.example.billionseconds.domain.auth.AuthErrorType.SessionExpired
                    ) {
                        dispatch(AppIntent.Auth.SessionExpired)
                    }
                }
            }
        }
    }

    fun dispatch(intent: AppIntent) {
        _state.update { AppReducer.reduce(it, intent) }
        when (intent) {
            is AppIntent.StartClicked               -> navigator.execute(NavCommand.Forward(AppScreen.OnboardingInput))
            is AppIntent.OnboardingCalculateClicked -> onboardingCalculate()
            is AppIntent.OnboardingContinueClicked  -> onboardingContinue()
            is AppIntent.CalculateClicked           -> mainCalculate()
            is AppIntent.ClearClicked               -> mainClear()
            is AppIntent.TabSelected                -> navigator.execute(NavCommand.SwitchTab(intent.tab))
            is AppIntent.CountdownScreenStarted     -> onCountdownResumed()
            is AppIntent.CountdownScreenResumed     -> onCountdownResumed()
            is AppIntent.LifeStatsScreenStarted     -> onLifeStatsResumed()
            is AppIntent.LifeStatsScreenResumed     -> onLifeStatsResumed()
            is AppIntent.MilestonesScreenStarted    -> onMilestonesResumed()
            is AppIntent.MilestonesScreenResumed    -> onMilestonesResumed()
            is AppIntent.MilestoneShareClicked      -> onMilestoneShareClicked(intent.id)
            is AppIntent.FamilyScreenStarted        -> onFamilyScreenResumed()
            is AppIntent.FamilyScreenResumed        -> onFamilyScreenResumed()
            is AppIntent.AddProfileClicked          -> onFamilyAddProfileClicked()
            is AppIntent.EditProfileClicked         -> onFamilyEditProfileClicked(intent.id)
            is AppIntent.DeleteConfirmed            -> onFamilyDeleteConfirmed()
            is AppIntent.SetActiveProfileClicked    -> onFamilySetActiveProfile(intent.id)
            is AppIntent.FormSaveClicked            -> onFamilyFormSave()
            is AppIntent.ShareClicked               -> onShare()
            is AppIntent.CreateVideoClicked         -> emitEffect(AppEffect.ShowComingSoon("create_video"))
            is AppIntent.WriteLetterClicked         -> emitEffect(AppEffect.ShowComingSoon("write_letter"))
            is AppIntent.AddFamilyClicked           -> emitEffect(AppEffect.ShowComingSoon("add_family"))
            is AppIntent.BackClicked -> {
                when (navigator.current.value) {
                    is AppScreen.TimeCapsule -> {
                        val sub = _state.value.timeCapsule.subScreen
                        if (sub is TimeCapsuleSubScreen.List) {
                            navigator.execute(NavCommand.Back)
                        } else {
                            _state.update { it.copy(
                                timeCapsule = it.timeCapsule.copy(
                                    subScreen = TimeCapsuleSubScreen.List,
                                    formDraft = null,
                                    openedCapsule = null
                                )
                            )}
                        }
                    }
                    is AppScreen.Main -> {
                        val profileSubScreen = _state.value.profile.subScreen
                        if (profileSubScreen !is ProfileSubScreen.Root) {
                            _state.update {
                                it.copy(profile = it.profile.copy(
                                    subScreen = ProfileSubScreen.Root,
                                    confirmDialog = null
                                ))
                            }
                        } else {
                            emitEffect(AppEffect.ExitApp)
                        }
                    }
                    else -> {
                        if (navigator.canGoBack) navigator.execute(NavCommand.Back)
                        else emitEffect(AppEffect.ExitApp)
                    }
                }
            }
            // Profile
            is AppIntent.ProfileScreenStarted       -> onProfileResumed()
            is AppIntent.ProfileScreenResumed       -> onProfileResumed()
            is AppIntent.ActiveProfileSummaryClicked -> emitEffect(AppEffect.NavigateToFamily)
            is AppIntent.PremiumClicked             -> emitEffect(AppEffect.ShowComingSoon("premium"))
            is AppIntent.TimeCapsuleClicked         -> navigateToTimeCapsule()
            is AppIntent.HelpClicked                -> emitEffect(AppEffect.ShowComingSoon("help"))
            is AppIntent.LegalLinkClicked           -> onLegalLinkClicked(intent.type)
            is AppIntent.NotificationsToggled,
            is AppIntent.MilestoneRemindersToggled,
            is AppIntent.FamilyRemindersToggled,
            is AppIntent.ReengagementToggled,
            is AppIntent.ApproximateLabelsToggled,
            is AppIntent.Use24HourFormatToggled     -> persistSettings()
            is AppIntent.ConfirmDangerousAction     -> onConfirmDangerousAction()
            // Sync
            is AppIntent.SyncCompleted              -> onSyncCompleted()
            // Auth
            is AppIntent.Auth.ScreenOpened          -> onAuthScreenOpened(intent.source)
            is AppIntent.Auth.SignInWithGoogleClicked -> emitEffect(AppEffect.LaunchGoogleSignIn)
            is AppIntent.Auth.SignInWithAppleClicked  -> emitEffect(AppEffect.LaunchAppleSignIn)
            is AppIntent.Auth.ContinueAsGuestClicked  -> onAuthDismissed()
            is AppIntent.Auth.GoogleTokenReceived     -> onGoogleTokenReceived(intent.idToken)
            is AppIntent.Auth.AppleTokenReceived      -> onAppleTokenReceived(intent.identityToken, intent.name)
            is AppIntent.Auth.SignInFailed            -> onAuthFailed(intent.error)
            is AppIntent.Auth.LogoutClicked           -> Unit // reducer sets confirmDialog = SignOut
            is AppIntent.Auth.LogoutConfirmed         -> onLogoutConfirmed()
            is AppIntent.Auth.DismissError            -> _state.update { it.copy(auth = it.auth.copy(error = null)) }
            is AppIntent.Auth.SessionExpired          -> onSessionExpired()
            // Debug
            is AppIntent.DebugOpenEventScreen       -> onDebugOpenEventScreen()
            // Event Screen
            is AppIntent.Event.ScreenOpened -> {
                navigator.execute(NavCommand.Forward(AppScreen.EventScreen(intent.profileId, intent.source)))
                onEventScreenOpened(intent.profileId, intent.source)
            }
            is AppIntent.Event.MarkSeenIfNeeded     -> onEventMarkSeen()
            is AppIntent.Event.MarkCelebrationShownIfNeeded -> onEventMarkCelebrationShown()
            is AppIntent.Event.CelebrationCompleted -> onEventCelebrationCompleted()
            is AppIntent.Event.CelebrationSkipped   -> onEventMarkCelebrationShown()
            is AppIntent.Event.ShareClicked         -> onEventShareClicked()
            is AppIntent.Event.CreateVideoClicked   -> emitEffect(AppEffect.ShowComingSoon("create_video"))
            is AppIntent.Event.OpenMilestonesClicked -> emitEffect(AppEffect.NavigateToMilestonesFromEvent)
            is AppIntent.Event.OpenStatsClicked     -> emitEffect(AppEffect.NavigateToStatsFromEvent)
            is AppIntent.Event.GoHomeClicked        -> navigator.execute(NavCommand.NewRoot(AppScreen.Main()))
            is AppIntent.Event.NextMilestoneClicked -> emitEffect(AppEffect.ShowComingSoon("next_milestone"))
            is AppIntent.Event.RetryClicked         -> onEventRetry()
            is AppIntent.Event.ProfileChanged       -> onEventProfileChanged(intent.newProfileId)
            is AppIntent.Event.BackPressed,
            is AppIntent.Event.DismissClicked -> {
                val s = _state.value
                if (s.event.isBackAllowed || s.event.mode == EventMode.REPEAT) {
                    navigator.execute(NavCommand.Back)
                }
            }
            // Time Capsule Screen
            is AppIntent.TimeCapsule.ScreenStarted         -> loadTimeCapsules()
            is AppIntent.TimeCapsule.EditClicked           -> openTimeCapsuleForEdit(intent.id)
            is AppIntent.TimeCapsule.OpenClicked           -> openTimeCapsule(intent.id)
            is AppIntent.TimeCapsule.ConfirmDelete         -> deleteTimeCapsule(intent.id)
            is AppIntent.TimeCapsule.FormSaveClicked       -> saveTimeCapsule(isDraft = false)
            is AppIntent.TimeCapsule.FormSaveDraftClicked  -> saveTimeCapsule(isDraft = true)
            is AppIntent.TimeCapsule.BackClicked -> {
                val sub = _state.value.timeCapsule.subScreen
                if (sub is TimeCapsuleSubScreen.List) {
                    navigator.execute(NavCommand.Back)
                }
                // Остальные случаи (Create/Edit/Open) обрабатываются в reducer
            }
            else -> Unit
        }
    }

    // ── Sync ──────────────────────────────────────────────────────────────────

    private fun onSyncCompleted() {
        // Re-build state from local storage after server data has been applied
        val now = currentInstant()
        val activeProfile = getActiveProfileOrFallback() ?: return
        val birthData = activeProfile.toBirthdayData()
        val result = BillionSecondsCalculator.computeAll(birthData, now)
        _state.update { current ->
            current.copy(
                year               = activeProfile.birthYear,
                month              = activeProfile.birthMonth,
                day                = activeProfile.birthDay,
                hour               = activeProfile.birthHour,
                minute             = activeProfile.birthMinute,
                unknownTime        = activeProfile.unknownBirthTime,
                milestoneInstant   = result.milestoneInstant,
                progressPercent    = result.progressPercent,
                isMilestoneReached = result.isMilestoneReached,
                secondsRemaining   = result.secondsRemaining,
                showMainResult     = true,
                countdown  = buildCountdownUiState(result, activeProfile.unknownBirthTime, now, activeProfile.name),
                lifeStats  = buildLifeStatsUiState(birthData, result, activeProfile.unknownBirthTime, now),
                milestones = buildMilestonesUiState(birthData, activeProfile.unknownBirthTime, now),
                family     = buildFamilyUiState(now),
                profile    = buildProfileUiState(now)
            )
        }
        startTick(result.milestoneInstant)
    }

    // ── Onboarding ────────────────────────────────────────────────────────────

    private fun onboardingCalculate() {
        val s = _state.value
        val error = BirthdayValidator.validate(s.year, s.month, s.day, currentInstant())
        if (error != null) {
            val message = when (error) {
                is BirthdayValidator.ValidationError.DateRequired   -> "Введите дату рождения"
                is BirthdayValidator.ValidationError.DateInFuture   -> "Дата рождения не может быть в будущем"
                is BirthdayValidator.ValidationError.DateInvalidDay -> "Некорректная дата"
                is BirthdayValidator.ValidationError.YearOutOfRange -> "Некорректный год (минимум 1900)"
            }
            _state.update { it.copy(error = message) }
            return
        }

        try {
            val data = BirthdayData(s.year!!, s.month!!, s.day!!, s.hour, s.minute)
            val now = currentInstant()
            val result = BillionSecondsCalculator.computeAll(data, now)
            repository.saveBirthday(data)
            repository.setUnknownTime(s.unknownTime)
            _state.update {
                it.copy(
                    milestoneInstant   = result.milestoneInstant,
                    progressPercent    = result.progressPercent,
                    isMilestoneReached = result.isMilestoneReached,
                    secondsRemaining   = result.secondsRemaining,
                    error              = null
                )
            }
            navigator.execute(NavCommand.Forward(AppScreen.OnboardingResult))
        } catch (e: Exception) {
            _state.update { it.copy(error = "Некорректная дата") }
        }
    }

    private fun onboardingContinue() {
        repository.setOnboardingCompleted(true)
        val s = _state.value
        val milestone = s.milestoneInstant ?: return
        val now = currentInstant()
        val data = BirthdayData(s.year!!, s.month!!, s.day!!, s.hour, s.minute)

        // Создаём primary FamilyProfile из данных онбординга
        migrateLegacyBirthdayToFamily()

        val result = BillionSecondsCalculator.computeAll(data, now)
        _state.update {
            it.copy(
                showMainResult = true,
                countdown    = buildCountdownUiState(result, s.unknownTime, now),
                lifeStats    = buildLifeStatsUiState(data, result, s.unknownTime, now),
                milestones   = buildMilestonesUiState(data, s.unknownTime, now),
                family       = buildFamilyUiState(now),
                profile      = buildProfileUiState(now)
            )
        }
        navigator.execute(NavCommand.NewRoot(AppScreen.Main(MainTab.Home)))
        startTick(milestone)
    }

    // ── Main (legacy) ─────────────────────────────────────────────────────────

    private fun mainCalculate() {
        val s = _state.value
        val year  = s.year  ?: run { _state.update { it.copy(error = "Введите дату") }; return }
        val month = s.month ?: run { _state.update { it.copy(error = "Введите дату") }; return }
        val day   = s.day   ?: run { _state.update { it.copy(error = "Введите дату") }; return }

        try {
            val data = BirthdayData(year, month, day, s.hour, s.minute)
            val now  = currentInstant()
            val result = BillionSecondsCalculator.computeAll(data, now)
            repository.saveBirthday(data)
            _state.update {
                it.copy(
                    milestoneInstant   = result.milestoneInstant,
                    progressPercent    = result.progressPercent,
                    isMilestoneReached = result.isMilestoneReached,
                    secondsRemaining   = result.secondsRemaining,
                    showMainResult     = true,
                    error              = null,
                    countdown  = buildCountdownUiState(result, s.unknownTime, now),
                    lifeStats  = buildLifeStatsUiState(data, result, s.unknownTime, now),
                    milestones = buildMilestonesUiState(data, s.unknownTime, now)
                )
            }
            startTick(result.milestoneInstant)
        } catch (e: Exception) {
            _state.update { it.copy(error = "Некорректная дата") }
        }
    }

    private fun mainClear() {
        tickJob?.cancel()
        repository.clearBirthday()
        repository.setOnboardingCompleted(true)
    }

    // ── Countdown screen ──────────────────────────────────────────────────────

    private fun onCountdownResumed() {
        val activeProfile = getActiveProfileOrFallback() ?: return
        val now = currentInstant()
        val birthData = activeProfile.toBirthdayData()
        val result = BillionSecondsCalculator.computeAll(birthData, now)
        _state.update {
            it.copy(countdown = buildCountdownUiState(result, activeProfile.unknownBirthTime, now, activeProfile.name))
        }
    }

    // ── Life Stats screen ─────────────────────────────────────────────────────

    private fun onLifeStatsResumed() {
        val activeProfile = getActiveProfileOrFallback() ?: run {
            _state.update { it.copy(lifeStats = LifeStatsUiState(isLoading = false, error = LifeStatsError.NoBirthData)) }
            return
        }
        val now = currentInstant()
        val birthData = activeProfile.toBirthdayData()
        val result = BillionSecondsCalculator.computeAll(birthData, now)
        _state.update {
            it.copy(lifeStats = buildLifeStatsUiState(birthData, result, activeProfile.unknownBirthTime, now))
        }
    }

    // ── Milestones screen ─────────────────────────────────────────────────────

    private fun onMilestonesResumed() {
        val activeProfile = getActiveProfileOrFallback() ?: run {
            _state.update {
                it.copy(milestones = MilestonesUiState(isLoading = false, error = MilestonesError.NoBirthData))
            }
            return
        }
        val now = currentInstant()
        val birthData = activeProfile.toBirthdayData()
        val unknownTime = activeProfile.unknownBirthTime
        val newState = buildMilestonesUiState(birthData, unknownTime, now)
        _state.update { it.copy(milestones = newState) }
        // Детект newly reached при открытии экрана
        newState.celebrationAvailableId?.let { id ->
            repository.setLastSeenMilestoneId(id)
            emitEffect(AppEffect.ShowMilestoneCelebration(id))
        }
    }

    private fun onMilestoneShareClicked(id: String) {
        val item = _state.value.milestones.milestones.firstOrNull { it.id == id } ?: return
        val text = "Я достиг вехи «${item.title}»! \uD83C\uDF89 #BillionSeconds"
        emitEffect(AppEffect.ShareMilestone(id, text))
    }

    // ── Family screen ─────────────────────────────────────────────────────────

    private fun onFamilyScreenResumed() {
        val now = currentInstant()
        _state.update { it.copy(family = buildFamilyUiState(now)) }
    }

    private fun onFamilyAddProfileClicked() {
        val profiles = familyRepository.loadProfiles()
        if (profiles.size >= MAX_PROFILES) {
            emitEffect(AppEffect.ShowFamilyError("Достигнут лимит профилей (максимум $MAX_PROFILES)"))
            return
        }
        _state.update {
            it.copy(family = it.family.copy(
                subScreen = FamilySubScreen.CreateForm,
                formDraft = ProfileFormDraft()
            ))
        }
    }

    private fun onFamilyEditProfileClicked(id: String) {
        val profile = familyRepository.getProfileById(id) ?: return
        _state.update {
            it.copy(family = it.family.copy(
                subScreen = FamilySubScreen.EditForm(id),
                formDraft = ProfileFormDraft(
                    profileId        = id,
                    name             = profile.name,
                    relationType     = profile.relationType,
                    customRelationName = profile.customRelationName ?: "",
                    year             = profile.birthYear,
                    month            = profile.birthMonth,
                    day              = profile.birthDay,
                    hour             = profile.birthHour,
                    minute           = profile.birthMinute,
                    unknownBirthTime = profile.unknownBirthTime
                )
            ))
        }
    }

    private fun onFamilyFormSave() {
        val draft = _state.value.family.formDraft ?: return
        val now = currentInstant()

        val nameError = FamilyProfileValidator.validateName(draft.name)
        val dateError = FamilyProfileValidator.validateBirthDate(draft.year, draft.month, draft.day, now)

        if (nameError != null || dateError != null) {
            _state.update {
                it.copy(family = it.family.copy(
                    formDraft = draft.copy(
                        nameError = nameError?.let { e -> FamilyProfileValidator.errorMessage(e) },
                        dateError = dateError?.let { e -> FamilyProfileValidator.errorMessage(e) }
                    )
                ))
            }
            return
        }

        val profiles = familyRepository.loadProfiles().toMutableList()
        val isCreate = draft.profileId == null
        val customName = if (draft.relationType == RelationType.OTHER)
            draft.customRelationName.trim().takeIf { it.isNotEmpty() }
        else null

        if (isCreate) {
            val newProfile = FamilyProfile(
                id                 = generateProfileId(),
                name               = draft.name.trim(),
                relationType       = draft.relationType,
                customRelationName = customName,
                birthYear          = draft.year!!,
                birthMonth         = draft.month!!,
                birthDay           = draft.day!!,
                birthHour          = draft.hour,
                birthMinute        = draft.minute,
                unknownBirthTime   = draft.unknownBirthTime,
                isPrimary          = false,
                sortOrder          = profiles.size,
                createdAtEpochSeconds = now.epochSeconds
            )
            profiles.add(newProfile)
        } else {
            val idx = profiles.indexOfFirst { it.id == draft.profileId }
            if (idx < 0) return
            profiles[idx] = profiles[idx].copy(
                name               = draft.name.trim(),
                relationType       = draft.relationType,
                customRelationName = customName,
                birthYear          = draft.year!!,
                birthMonth         = draft.month!!,
                birthDay           = draft.day!!,
                birthHour          = draft.hour,
                birthMinute        = draft.minute,
                unknownBirthTime   = draft.unknownBirthTime
            )
        }

        familyRepository.saveProfiles(profiles)

        // Если обновлён активный профиль — пересчитать все sub-states
        val activeId = familyRepository.getActiveProfileId()
        val updatedActiveProfile = if (!isCreate && draft.profileId == activeId) {
            profiles.firstOrNull { it.id == activeId }
        } else null

        if (updatedActiveProfile != null) {
            val birthData = updatedActiveProfile.toBirthdayData()
            val result = BillionSecondsCalculator.computeAll(birthData, now)
            val unknownTime = updatedActiveProfile.unknownBirthTime
            _state.update {
                it.copy(
                    countdown  = buildCountdownUiState(result, unknownTime, now, updatedActiveProfile.name),
                    lifeStats  = buildLifeStatsUiState(birthData, result, unknownTime, now),
                    milestones = buildMilestonesUiState(birthData, unknownTime, now),
                    family     = buildFamilyUiState(now).copy(
                        subScreen = FamilySubScreen.List,
                        formDraft = null
                    )
                )
            }
            startTick(result.milestoneInstant)
        } else {
            _state.update {
                it.copy(family = buildFamilyUiState(now).copy(
                    subScreen = FamilySubScreen.List,
                    formDraft = null
                ))
            }
        }
    }

    private fun onFamilyDeleteConfirmed() {
        val pendingId = _state.value.family.pendingDeleteId ?: return
        val profiles = familyRepository.loadProfiles().toMutableList()
        val toDelete = profiles.firstOrNull { it.id == pendingId } ?: return
        // Safety: нельзя удалить primary или единственный профиль
        if (toDelete.isPrimary || profiles.size <= 1) return

        profiles.removeAll { it.id == pendingId }
        familyRepository.saveProfiles(profiles)

        val activeId = familyRepository.getActiveProfileId()
        val now = currentInstant()

        if (activeId == pendingId) {
            // Auto-select primary (или первый оставшийся)
            val newActive = profiles.firstOrNull { it.isPrimary } ?: profiles.first()
            familyRepository.setActiveProfileId(newActive.id)
            val birthData = newActive.toBirthdayData()
            val result = BillionSecondsCalculator.computeAll(birthData, now)
            val unknownTime = newActive.unknownBirthTime
            _state.update {
                it.copy(
                    countdown  = buildCountdownUiState(result, unknownTime, now, newActive.name),
                    lifeStats  = buildLifeStatsUiState(birthData, result, unknownTime, now),
                    milestones = buildMilestonesUiState(birthData, unknownTime, now),
                    family     = buildFamilyUiState(now).copy(
                        pendingDeleteId = null,
                        isDeleteConfirmationVisible = false
                    )
                )
            }
            emitEffect(AppEffect.ActiveProfileChanged(newActive.id))
            startTick(result.milestoneInstant)
        } else {
            _state.update {
                it.copy(family = buildFamilyUiState(now).copy(
                    pendingDeleteId = null,
                    isDeleteConfirmationVisible = false
                ))
            }
        }
    }

    private fun onFamilySetActiveProfile(id: String) {
        val profile = familyRepository.getProfileById(id) ?: return
        familyRepository.setActiveProfileId(id)
        val now = currentInstant()
        val birthData = profile.toBirthdayData()
        val result = BillionSecondsCalculator.computeAll(birthData, now)
        val unknownTime = profile.unknownBirthTime
        _state.update {
            it.copy(
                countdown  = buildCountdownUiState(result, unknownTime, now, profile.name),
                lifeStats  = buildLifeStatsUiState(birthData, result, unknownTime, now),
                milestones = buildMilestonesUiState(birthData, unknownTime, now),
                family     = buildFamilyUiState(now)
            )
        }
        emitEffect(AppEffect.ActiveProfileChanged(id))
        startTick(result.milestoneInstant)
        // Сброс event auto-trigger для нового профиля
        dispatch(AppIntent.Event.ProfileChanged(id))
    }

    // ── Share ─────────────────────────────────────────────────────────────────

    private fun onShare() {
        val s = _state.value.countdown
        val text = if (s.eventStatus == com.example.billionseconds.domain.model.EventStatus.Reached) {
            "Я достиг миллиарда секунд жизни! \uD83C\uDF89 #BillionSeconds"
        } else {
            "Мой миллиард секунд наступит ${s.formattedMilestoneDate} в ${s.formattedMilestoneTime}. " +
            "Прогресс: ${s.formattedProgress} #BillionSeconds"
        }
        emitEffect(AppEffect.ShareText(text))
    }

    // ── Event Screen ──────────────────────────────────────────────────────────

    /**
     * Debug/test helper: открывает EventScreen в first-time режиме
     * для активного профиля, игнорируя реальную дату миллиарда секунд.
     *
     * НЕ трогает event history и дату рождения профиля.
     * Ничего не пишет в хранилище — только показывает экран.
     */
    private fun onDebugOpenEventScreen() {
        val profile = getActiveProfileOrFallback() ?: run {
            emitEffect(AppEffect.ShowError("Нет активного профиля"))
            return
        }
        val now = currentInstant()

        // Строим фейковую доменную модель с реальными данными профиля,
        // но форсируем isReached = true и mode = FIRST_TIME
        val fakeDomain = EventDomainModel(
            profileId         = profile.id,
            profileName       = profile.name,
            relationType      = profile.relationType,
            unknownBirthTime  = profile.unknownBirthTime,
            targetDateTime    = now,          // "событие наступило прямо сейчас"
            isReached         = true,
            wasShown          = false,
            celebrationShown  = false,
            sharePromptShown  = false,
            firstShownAt      = null,
            mode              = EventMode.FIRST_TIME,
            eligibilityStatus = EventEligibilityStatus.EligibleFirstTime,
            source            = EventSource.MANUAL,
            isApproximateMode = profile.unknownBirthTime
        )

        val uiModel = EventUiMapper.toUiModel(fakeDomain, now)

        _state.update {
            it.copy(
                event = it.event.copy(
                    isLoading            = false,
                    profileId            = profile.id,
                    profileName          = profile.name,
                    targetDateTime       = now,
                    firstShownAt         = null,
                    isApproximateMode    = profile.unknownBirthTime,
                    mode                 = EventMode.FIRST_TIME,
                    source               = EventSource.MANUAL,
                    screenStatus         = EventScreenStatus.FirstTime,
                    isBackAllowed        = false,
                    isCelebrationRunning = false,
                    celebrationCompleted = false,
                    uiModel              = uiModel,
                    errorMessage         = null
                )
            )
        }
        navigator.execute(NavCommand.Forward(AppScreen.EventScreen(profile.id, EventSource.MANUAL)))
        _state.update { it.copy(event = it.event.copy(triggerCelebration = true)) }
    }

    private fun onEventScreenOpened(profileId: String, source: EventSource) {
        val profile = familyRepository.getProfileById(profileId)
        if (profile == null) {
            _state.update {
                it.copy(event = it.event.copy(
                    isLoading = false,
                    screenStatus = EventScreenStatus.ProfileMissing,
                    errorMessage = "Профиль не найден"
                ))
            }
            return
        }
        val now = currentInstant()
        val domain = eventDataBuilder.build(profile, source, now)

        if (!domain.isReached) {
            _state.update {
                it.copy(event = it.event.copy(
                    isLoading = false,
                    screenStatus = EventScreenStatus.NotEligible,
                    errorMessage = "Событие ещё не наступило"
                ))
            }
            return
        }

        val uiModel = EventUiMapper.toUiModel(domain, now)
        val status = if (domain.mode == com.example.billionseconds.domain.event.model.EventMode.FIRST_TIME)
            EventScreenStatus.FirstTime else EventScreenStatus.Repeat
        val isBackAllowed = domain.mode == com.example.billionseconds.domain.event.model.EventMode.REPEAT

        _state.update {
            it.copy(event = it.event.copy(
                isLoading            = false,
                profileId            = profileId,
                profileName          = domain.profileName,
                targetDateTime       = domain.targetDateTime,
                firstShownAt         = domain.firstShownAt,
                isApproximateMode    = domain.isApproximateMode,
                mode                 = domain.mode,
                source               = source,
                screenStatus         = status,
                isBackAllowed        = isBackAllowed,
                isCelebrationRunning = false,
                celebrationCompleted = domain.celebrationShown,
                uiModel              = uiModel,
                errorMessage         = null
            ))
        }

        // Если first-time — сразу фиксируем факт показа и запускаем celebration
        if (domain.mode == com.example.billionseconds.domain.event.model.EventMode.FIRST_TIME) {
            eventHistoryManager.markSeen(profileId, now)
            if (!domain.celebrationShown) {
                _state.update { it.copy(event = it.event.copy(triggerCelebration = true)) }
            } else {
                // Celebration уже было (после process death) — back разрешён
                _state.update { it.copy(event = it.event.copy(isBackAllowed = true)) }
            }
        }
    }

    private fun onEventMarkSeen() {
        val profileId = _state.value.event.profileId.takeIf { it.isNotEmpty() } ?: return
        eventHistoryManager.markSeen(profileId, currentInstant())
    }

    private fun onEventMarkCelebrationShown() {
        val profileId = _state.value.event.profileId.takeIf { it.isNotEmpty() } ?: return
        eventHistoryManager.markCelebrationShown(profileId, currentInstant())
    }

    private fun onEventCelebrationCompleted() {
        onEventMarkCelebrationShown()
    }

    private fun onEventShareClicked() {
        val profileId = _state.value.event.profileId.takeIf { it.isNotEmpty() } ?: return
        val targetDateTime = _state.value.event.targetDateTime ?: return
        val profile = familyRepository.getProfileById(profileId) ?: return
        val payload = EventSharePayloadBuilder.build(profile, targetDateTime)
        emitEffect(AppEffect.ShareEventPayload(payload))
        // Фиксируем факт показа share prompt
        eventHistoryManager.markSharePromptShown(profileId, currentInstant())
    }

    private fun onEventRetry() {
        val profileId = _state.value.event.profileId.takeIf { it.isNotEmpty() } ?: return
        val source = _state.value.event.source
        onEventScreenOpened(profileId, source)
    }

    private fun onEventProfileChanged(newProfileId: String) {
        // autoOpenTriggered уже сброшен в reducer
        val activeProfile = familyRepository.getProfileById(newProfileId)
            ?: getActiveProfileOrFallback() ?: return
        val now = currentInstant()
        checkEventEligibilityAndTrigger(activeProfile, now)
    }

    /**
     * Проверяет eligibility события и автоматически открывает EventScreen.
     * Вызывается при старте приложения, при тике (только если !autoOpenTriggered),
     * и при смене active profile.
     */
    private fun checkEventEligibilityAndTrigger(profile: FamilyProfile, now: Instant) {
        val result = eventEligibilityChecker.check(profile, now)
        if (result.status == EventEligibilityStatus.EligibleFirstTime) {
            if (!_state.value.event.autoOpenTriggered) {
                _state.update { it.copy(event = it.event.copy(autoOpenTriggered = true)) }
                emitEffect(AppEffect.NavigateToEventScreen(profile.id, EventSource.AUTO))
            }
        }
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    private fun startTick(milestone: Instant) {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (true) {
                delay(1000)
                val now = currentInstant()
                val activeProfile = getActiveProfileOrFallback() ?: continue
                val birthData = activeProfile.toBirthdayData()
                val unknownTime = activeProfile.unknownBirthTime
                val result = BillionSecondsCalculator.computeAll(birthData, now)
                val newMilestones = buildMilestonesUiState(birthData, unknownTime, now)
                // Детект newly reached в тике
                val newlyReachedId = newMilestones.celebrationAvailableId
                if (newlyReachedId != null) {
                    repository.setLastSeenMilestoneId(newlyReachedId)
                    emitEffect(AppEffect.ShowMilestoneCelebration(newlyReachedId))
                }
                _state.update {
                    it.copy(
                        isMilestoneReached = result.isMilestoneReached,
                        secondsRemaining   = result.secondsRemaining,
                        countdown  = buildCountdownUiState(result, unknownTime, now, activeProfile.name),
                        lifeStats  = buildLifeStatsUiState(birthData, result, unknownTime, now),
                        milestones = if (newlyReachedId != null)
                            newMilestones.copy(celebrationAvailableId = null)
                        else newMilestones
                    )
                }
                // Проверка event eligibility в тике — только если auto-trigger ещё не срабатывал
                if (!_state.value.event.autoOpenTriggered) {
                    checkEventEligibilityAndTrigger(activeProfile, now)
                }
            }
        }
    }

    // ── Migration ─────────────────────────────────────────────────────────────

    private fun migrateLegacyBirthdayToFamily() {
        if (familyRepository.loadProfiles().isNotEmpty()) return
        val legacy = repository.getBirthday() ?: return
        val unknownTime = repository.isUnknownTime()
        val primaryProfile = FamilyProfile(
            id                    = PRIMARY_PROFILE_ID,
            name                  = "Я",
            relationType          = RelationType.SELF,
            birthYear             = legacy.year,
            birthMonth            = legacy.month,
            birthDay              = legacy.day,
            birthHour             = legacy.hour,
            birthMinute           = legacy.minute,
            unknownBirthTime      = unknownTime,
            isPrimary             = true,
            sortOrder             = 0,
            createdAtEpochSeconds = currentInstant().epochSeconds
        )
        familyRepository.saveProfiles(listOf(primaryProfile))
        familyRepository.setActiveProfileId(primaryProfile.id)
    }

    private fun getActiveProfileOrFallback(): FamilyProfile? {
        val profiles = familyRepository.loadProfiles()
        if (profiles.isEmpty()) return null
        val activeId = familyRepository.getActiveProfileId()
        return profiles.firstOrNull { it.id == activeId }
            ?: profiles.firstOrNull { it.isPrimary }
            ?: profiles.first()
    }

    private fun generateProfileId(): String {
        val timestamp = currentInstant().epochSeconds
        val random = kotlin.random.Random.nextLong().let { if (it < 0) -it else it }
        return "${timestamp}_$random"
    }

    // ── Builders ──────────────────────────────────────────────────────────────

    private fun buildCountdownUiState(
        result: MilestoneResult,
        unknownTime: Boolean,
        now: Instant,
        activeProfileName: String? = null
    ): CountdownUiState = CountdownUiState(
        isLoading           = false,
        eventStatus         = result.toEventStatus(now),
        milestoneInstant    = result.milestoneInstant,
        progressFraction    = result.progressPercent,
        secondsRemaining    = result.secondsRemaining,
        isUnknownBirthTime  = unknownTime,
        formattedMilestoneDate = CountdownFormatter.formatMilestoneDate(result.milestoneInstant),
        formattedMilestoneTime = CountdownFormatter.formatMilestoneTime(result.milestoneInstant),
        formattedCountdown  = CountdownFormatter.formatCountdown(result.secondsRemaining),
        formattedProgress   = CountdownFormatter.formatProgress(result.progressPercent),
        activeProfileName   = activeProfileName,
        error               = null
    )

    private fun buildLifeStatsUiState(
        data: BirthdayData,
        result: MilestoneResult,
        unknownTime: Boolean,
        now: Instant
    ): LifeStatsUiState {
        val raw = LifeStatsCalculator.compute(
            birthData        = data,
            now              = now,
            isUnknownTime    = unknownTime,
            progressFraction = result.progressPercent,
            secondsRemaining = result.secondsRemaining
        )

        val approxDisclaimer = if (unknownTime) "Время рождения не указано — расчёт приблизительный" else null

        val exactStats = listOf(
            StatItem(
                id    = "seconds_lived",
                title = "Секунд прожито",
                value = LifeStatsFormatter.formatLarge(raw.secondsLived)
            ),
            StatItem(
                id    = "minutes_lived",
                title = "Минут прожито",
                value = LifeStatsFormatter.formatLarge(raw.minutesLived)
            ),
            StatItem(
                id    = "hours_lived",
                title = "Часов прожито",
                value = LifeStatsFormatter.formatLarge(raw.hoursLived)
            ),
            StatItem(
                id    = "days_lived",
                title = "Дней прожито",
                value = LifeStatsFormatter.formatLarge(raw.daysLived)
            ),
            StatItem(
                id    = "weeks_lived",
                title = "Недель прожито",
                value = LifeStatsFormatter.formatLarge(raw.weeksLived)
            ),
            StatItem(
                id    = "progress_billion",
                title = "Прогресс к миллиарду",
                value = LifeStatsFormatter.formatPercent(raw.progressToBillion * 100f)
            ),
            StatItem(
                id    = "days_to_billion",
                title = if (raw.secondsRemaining > 0L) "Дней до миллиарда" else "Дней назад",
                value = LifeStatsFormatter.formatLarge(raw.secondsRemaining / 86_400L)
            )
        )

        val approximateStats = listOf(
            StatItem(
                id           = "heartbeats",
                title        = "Ударов сердца",
                value        = LifeStatsFormatter.formatApproxBillions(raw.heartbeats),
                isApproximate = true,
                disclaimer   = approxDisclaimer ?: "Из расчёта ${LifeStatsCalculator.HEART_RATE_BPM} уд/мин"
            ),
            StatItem(
                id           = "sleep_days",
                title        = "Дней сна",
                value        = LifeStatsFormatter.formatApprox(raw.sleepDays),
                isApproximate = true,
                disclaimer   = approxDisclaimer ?: "Из расчёта ${LifeStatsCalculator.SLEEP_HOURS_PER_DAY.toInt()} ч/сутки"
            ),
            StatItem(
                id           = "life_progress",
                title        = "Прожито от средней жизни",
                value        = LifeStatsFormatter.formatPercent(raw.lifeProgressPct),
                isApproximate = true,
                disclaimer   = approxDisclaimer ?: "Базовая продолжительность ${LifeStatsCalculator.LIFE_EXPECTANCY_YEARS.toInt()} лет"
            )
        )

        return LifeStatsUiState(
            isLoading          = false,
            isUnknownBirthTime = unknownTime,
            ageLabel           = LifeStatsFormatter.formatAge(raw.ageYears, raw.ageMonths, raw.ageDays),
            exactStats         = exactStats,
            approximateStats   = approximateStats,
            error              = null
        )
    }

    private fun buildMilestonesUiState(
        data: BirthdayData,
        unknownTime: Boolean,
        now: Instant
    ): MilestonesUiState {
        val lastSeenId = repository.getLastSeenMilestoneId()
        val calcResult = MilestonesCalculator.compute(
            birthData        = data,
            now              = now,
            isUnknownTime    = unknownTime,
            lastSeenReachedId = lastSeenId
        )

        val items = calcResult.milestones.map { um ->
            val isApprox = unknownTime
            MilestoneUiItem(
                id          = um.definition.id,
                title       = um.definition.title,
                shortTitle  = um.definition.shortTitle,
                targetDateText = MilestonesFormatter.formatTargetDate(um.targetInstant, isApprox),
                statusLabel = when (um.status) {
                    is MilestoneStatus.Reached  -> "Достигнуто"
                    is MilestoneStatus.Next     -> "Следующее"
                    is MilestoneStatus.Upcoming -> "Впереди"
                },
                progressText = when (um.status) {
                    is MilestoneStatus.Next -> MilestonesFormatter.formatProgress(um.progressFromPrev)
                    else                    -> ""
                },
                remainingText = when (um.status) {
                    is MilestoneStatus.Next -> "осталось ${MilestonesFormatter.formatRemaining(um.secondsRemaining)}"
                    else                    -> ""
                },
                reachedDateText = when (um.status) {
                    is MilestoneStatus.Reached -> MilestonesFormatter.formatReachedDate(um.targetInstant)
                    else                       -> ""
                },
                status       = um.status,
                isPrimary    = um.definition.isPrimary,
                isShareable  = um.definition.isShareable,
                hasApproximateDisclaimer = isApprox
            )
        }

        val highlightedId = calcResult.nextMilestoneId ?: calcResult.lastReachedId

        return MilestonesUiState(
            isLoading              = false,
            milestones             = items,
            highlightedId          = highlightedId,
            isApproximateMode      = unknownTime,
            celebrationAvailableId = calcResult.newlyReachedId,
            error                  = null
        )
    }

    private fun buildFamilyUiState(now: Instant): FamilyUiState {
        val profiles = familyRepository.loadProfiles()
        val activeId = familyRepository.getActiveProfileId()

        val items = profiles.map { profile ->
            val calcResult = FamilyProfileCalculator.compute(profile, now)
            val isApprox = profile.unknownBirthTime
            FamilyProfileUiItem(
                id              = profile.id,
                name            = profile.name,
                relationLabel   = FamilyProfileFormatter.formatRelation(profile.relationType, profile.customRelationName),
                relationEmoji   = profile.relationType.emoji,
                birthDateText   = FamilyProfileFormatter.formatBirthDate(profile.birthYear, profile.birthMonth, profile.birthDay),
                billionDateText = FamilyProfileFormatter.formatBillionDate(calcResult.milestoneInstant, isApprox),
                progressText    = FamilyProfileFormatter.formatProgress(calcResult.progressPercent),
                countdownText   = FamilyProfileFormatter.formatCountdown(calcResult.secondsRemaining, calcResult.isMilestoneReached),
                isActive        = profile.id == activeId,
                isPrimary       = profile.isPrimary,
                isDeletable     = !profile.isPrimary && profiles.size > 1,
                isEditable      = true,
                hasApproximateTime = isApprox
            )
        }.sortedWith(compareBy({ !it.isPrimary }, { profiles.indexOfFirst { p -> p.id == it.id } }))

        return FamilyUiState(
            isLoading              = false,
            profiles               = items,
            activeProfileId        = activeId,
            canAddMore             = profiles.size < MAX_PROFILES,
            maxProfilesReached     = profiles.size >= MAX_PROFILES,
            error                  = null
        )
    }

    // ── Profile screen ────────────────────────────────────────────────────────

    private fun onProfileResumed() {
        val now = currentInstant()
        _state.update { it.copy(profile = buildProfileUiState(now)) }
    }

    private fun persistSettings() {
        // Reducer уже обновил state; читаем новые settings и сохраняем
        settingsRepository.saveSettings(_state.value.profile.settings)
    }

    private fun onLegalLinkClicked(type: LegalLinkType) {
        val url = when (type) {
            LegalLinkType.PrivacyPolicy -> PRIVACY_POLICY_URL
            LegalLinkType.TermsOfUse   -> TERMS_URL
        }
        emitEffect(AppEffect.LaunchExternalUrl(url))
    }

    private fun onConfirmDangerousAction() {
        if (_state.value.profile.isActionInProgress) return  // guard double-tap
        val dialog = _state.value.profile.confirmDialog ?: return
        scope.launch {
            when (dialog) {
                ProfileConfirmDialog.SignOut -> {
                    _state.update { it.copy(profile = it.profile.copy(confirmDialog = null, isActionInProgress = false)) }
                    dispatch(AppIntent.Auth.LogoutConfirmed)
                }
                ProfileConfirmDialog.ResetOnboarding,
                ProfileConfirmDialog.ClearAllData -> {
                    tickJob?.cancel()
                    repository.clearBirthday()
                    repository.setOnboardingCompleted(false)
                    familyRepository.clearAll()
                    settingsRepository.clearAll()
                    _state.value = AppState()
                    navigator.execute(NavCommand.NewRoot(AppScreen.OnboardingIntro))
                    emitEffect(AppEffect.OnboardingReset)
                }
            }
        }
    }

    private fun buildProfileUiState(now: Instant): ProfileUiState {
        val activeProfile = getActiveProfileOrFallback()
        val settings = settingsRepository.getSettings()
        val summary = activeProfile?.let {
            val calc = FamilyProfileCalculator.compute(it, now)
            ActiveProfileSummary(
                name               = it.name,
                relationLabel      = FamilyProfileFormatter.formatRelation(it.relationType, it.customRelationName),
                relationEmoji      = it.relationType.emoji,
                billionDateText    = FamilyProfileFormatter.formatBillionDate(calc.milestoneInstant, it.unknownBirthTime),
                hasApproximateTime = it.unknownBirthTime,
                isPrimary          = it.isPrimary
            )
        }
        return ProfileUiState(
            isLoading            = false,
            activeProfileSummary = summary,
            settings             = settings,
            appVersion           = AppMetaProvider.getVersion(),
            subScreen            = _state.value.profile.subScreen,
            confirmDialog        = _state.value.profile.confirmDialog,
            isActionInProgress   = _state.value.profile.isActionInProgress,
            error                = null,
            authState            = authManager?.authState?.value
                                    ?: com.example.billionseconds.domain.auth.AuthState.Unauthenticated,
        )
    }

    // ── Auth ─────────────────────────────────────────────────────────────────

    private fun onAuthScreenOpened(source: AuthSource) {
        _state.update { it.copy(auth = AuthUiState(source = source)) }
        navigator.execute(NavCommand.Forward(AppScreen.AuthEntry(source)))
    }

    private fun onAuthDismissed() {
        if (navigator.canGoBack) navigator.execute(NavCommand.Back)
        emitEffect(AppEffect.DismissAuthScreen)
    }

    private fun onGoogleTokenReceived(idToken: String) {
        if (authManager == null) { onAuthDismissed(); return }
        _state.update { it.copy(auth = it.auth.copy(isGoogleLoading = true, error = null)) }
        val source = _state.value.auth.source
        scope.launch {
            authManager.signInWithGoogle(idToken)
                .onSuccess {
                    _state.update { it.copy(auth = it.auth.copy(isGoogleLoading = false)) }
                    onAuthSuccess(source)
                }
                .onFailure {
                    val errorState = authManager.authState.value
                    val errorType = (errorState as? com.example.billionseconds.domain.auth.AuthState.Error)?.type
                        ?: com.example.billionseconds.domain.auth.AuthErrorType.Unknown("sign_in_failed")
                    _state.update { it.copy(auth = it.auth.copy(isGoogleLoading = false, error = errorType)) }
                }
        }
    }

    private fun onAppleTokenReceived(identityToken: String, name: String?) {
        if (authManager == null) { onAuthDismissed(); return }
        _state.update { it.copy(auth = it.auth.copy(isAppleLoading = true, error = null)) }
        val source = _state.value.auth.source
        scope.launch {
            authManager.signInWithApple(identityToken, name)
                .onSuccess {
                    _state.update { it.copy(auth = it.auth.copy(isAppleLoading = false)) }
                    onAuthSuccess(source)
                }
                .onFailure {
                    val errorState = authManager.authState.value
                    val errorType = (errorState as? com.example.billionseconds.domain.auth.AuthState.Error)?.type
                        ?: com.example.billionseconds.domain.auth.AuthErrorType.Unknown("sign_in_failed")
                    _state.update { it.copy(auth = it.auth.copy(isAppleLoading = false, error = errorType)) }
                }
        }
    }

    private fun onAuthSuccess(source: AuthSource) {
        // Dismiss auth screen
        if (navigator.canGoBack) navigator.execute(NavCommand.Back)
        emitEffect(AppEffect.DismissAuthScreen)

        // Refresh profile so AuthAccountSection shows the signed-in user immediately
        _state.update { it.copy(profile = buildProfileUiState(currentInstant())) }

        // Source-specific post-auth actions
        when (source) {
            AuthSource.SYNC,
            AuthSource.BACKUP,
            AuthSource.CLOUD_FEATURE -> {
                if (syncManager != null) {
                    scope.launch(Dispatchers.Default) {
                        syncManager.syncOnStart()
                        dispatch(AppIntent.SyncCompleted)
                    }
                }
            }
            AuthSource.PROFILE,
            AuthSource.PREMIUM -> Unit
        }

        emitEffect(AppEffect.AuthSuccess)
    }

    private fun onAuthFailed(error: com.example.billionseconds.domain.auth.AuthErrorType) {
        _state.update {
            it.copy(auth = it.auth.copy(isGoogleLoading = false, isAppleLoading = false, error = error))
        }
    }

    private fun onLogoutConfirmed() {
        if (authManager == null) return
        scope.launch {
            authManager.signOut()
            // Refresh profile so AuthAccountSection shows Unauthenticated state immediately
            _state.update { it.copy(profile = buildProfileUiState(currentInstant())) }
        }
    }

    private fun onSessionExpired() {
        // Refresh profile — authManager.authState is now Error(SessionExpired) → isSignedIn = false
        _state.update { it.copy(profile = buildProfileUiState(currentInstant())) }
        // Close auth screen if it's open (e.g. user was re-authenticating)
        if (navigator.canGoBack) navigator.execute(NavCommand.Back)
        emitEffect(AppEffect.SessionExpiredBanner)
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun emitEffect(effect: AppEffect) {
        scope.launch { _effect.emit(effect) }
    }

    fun dispose() = scope.cancel()

    // ── Time Capsule ──────────────────────────────────────────────────────────

    private fun navigateToTimeCapsule() {
        navigator.execute(NavCommand.Forward(AppScreen.TimeCapsule))
    }

    private fun loadTimeCapsules() {
        val capsules = timeCapsuleRepository.getAll()
        val nowMs = currentInstant().toEpochMilliseconds()
        val profiles = familyRepository.loadProfiles()
        val items = capsules.map { capsule ->
            val status = CapsuleUnlockResolver.resolve(capsule, nowMs, profiles)
            CapsuleUiMapper.toUiItem(capsule, status, profiles)
        }
        val groups = CapsuleListGrouper.group(items)
        _state.update { it.copy(
            timeCapsule = it.timeCapsule.copy(
                isLoading = false,
                groups = groups,
                error = null
            )
        )}
    }

    private fun openTimeCapsule(id: String) {
        val capsule = timeCapsuleRepository.getAll().firstOrNull { it.id == id } ?: return
        val nowMs = currentInstant().toEpochMilliseconds()
        val profiles = familyRepository.loadProfiles()
        val status = CapsuleUnlockResolver.resolve(capsule, nowMs, profiles)
        if (status is CapsuleStatus.Available) {
            timeCapsuleRepository.markOpened(id)
            val opened = capsule.copy(openedAt = nowMs)
            _state.update { it.copy(
                timeCapsule = it.timeCapsule.copy(
                    subScreen = TimeCapsuleSubScreen.Open(id),
                    openedCapsule = opened
                )
            )}
            loadTimeCapsules()
        } else if (status is CapsuleStatus.Opened) {
            _state.update { it.copy(
                timeCapsule = it.timeCapsule.copy(
                    subScreen = TimeCapsuleSubScreen.Open(id),
                    openedCapsule = capsule
                )
            )}
        }
    }

    private fun openTimeCapsuleForEdit(id: String) {
        val capsule = timeCapsuleRepository.getAll().firstOrNull { it.id == id } ?: return
        val draft = capsuleToFormDraft(capsule)
        _state.update { it.copy(
            timeCapsule = it.timeCapsule.copy(
                subScreen = TimeCapsuleSubScreen.Edit(id),
                formDraft = draft
            )
        )}
    }

    private fun capsuleToFormDraft(capsule: TimeCapsule): CapsuleFormDraft {
        val conditionType: ConditionType
        var year = ""; var month = ""; var day = ""; var hour = "12"; var minute = "00"
        var profileId: String? = null

        when (val c = capsule.unlockCondition) {
            is UnlockCondition.ExactDateTime -> {
                conditionType = ConditionType.DATE
                val dt = kotlinx.datetime.Instant.fromEpochMilliseconds(c.epochMillis)
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                year   = dt.year.toString()
                month  = dt.monthNumber.toString().padStart(2, '0')
                day    = dt.dayOfMonth.toString().padStart(2, '0')
                hour   = dt.hour.toString().padStart(2, '0')
                minute = dt.minute.toString().padStart(2, '0')
            }
            is UnlockCondition.BillionSecondsEvent -> {
                conditionType = ConditionType.BILLION_SECONDS_EVENT
                profileId = c.profileId
            }
        }

        return CapsuleFormDraft(
            id = capsule.id,
            title = capsule.title,
            message = capsule.message,
            recipientProfileId = capsule.recipientProfileId,
            conditionType = conditionType,
            selectedYear = year,
            selectedMonth = month,
            selectedDay = day,
            selectedHour = hour,
            selectedMinute = minute,
            selectedProfileId = profileId,
            isDirty = false
        )
    }

    private fun deleteTimeCapsule(id: String) {
        try {
            timeCapsuleRepository.delete(id)
            loadTimeCapsules()
        } catch (e: Exception) {
            _state.update { it.copy(timeCapsule = it.timeCapsule.copy(error = TimeCapsuleError.DeleteFailed)) }
        }
    }

    private fun saveTimeCapsule(isDraft: Boolean) {
        val draft = _state.value.timeCapsule.formDraft ?: return
        val nowMs = currentInstant().toEpochMilliseconds()

        if (!isDraft) {
            val validation = TimeCapsuleValidator.validate(draft, nowMs)
            if (!validation.isValid) {
                _state.update { it.copy(
                    timeCapsule = it.timeCapsule.copy(
                        formDraft = it.timeCapsule.formDraft?.copy(
                            titleError = validation.titleError,
                            messageError = validation.messageError,
                            conditionError = validation.conditionError
                        )
                    )
                )}
                return
            }
        }

        val unlockCondition: UnlockCondition = when (draft.conditionType) {
            ConditionType.DATE -> {
                val ms = TimeCapsuleValidator.parseDateToMs(
                    draft.selectedYear, draft.selectedMonth, draft.selectedDay,
                    draft.selectedHour, draft.selectedMinute
                ) ?: return
                UnlockCondition.ExactDateTime(ms)
            }
            ConditionType.BILLION_SECONDS_EVENT -> {
                val profileId = draft.selectedProfileId ?: return
                UnlockCondition.BillionSecondsEvent(profileId)
            }
        }

        val capsule = TimeCapsule(
            id = draft.id ?: timeCapsuleRepository.generateId(),
            title = draft.title.trim(),
            message = draft.message.trim(),
            recipientProfileId = draft.recipientProfileId,
            unlockCondition = unlockCondition,
            createdAt = nowMs,
            isDraft = isDraft
        )

        try {
            timeCapsuleRepository.save(capsule)
            _state.update { it.copy(
                timeCapsule = it.timeCapsule.copy(
                    subScreen = TimeCapsuleSubScreen.List,
                    formDraft = null
                )
            )}
            loadTimeCapsules()
        } catch (e: Exception) {
            _state.update { it.copy(timeCapsule = it.timeCapsule.copy(error = TimeCapsuleError.SaveFailed)) }
        }
    }
}
