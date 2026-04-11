package com.example.billionseconds.mvi

import com.example.billionseconds.data.AppSettingsRepository
import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.FamilyProfileRepository
import com.example.billionseconds.data.event.EventHistoryRepository
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
import com.example.billionseconds.mvi.event.EventScreenStatus
import com.example.billionseconds.navigation.AppScreen
import com.example.billionseconds.navigation.MainTab
import com.example.billionseconds.util.AppMetaProvider
import com.example.billionseconds.util.currentInstant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant

private const val MAX_PROFILES = 5
private const val PRIMARY_PROFILE_ID = "self_primary"

private const val PRIVACY_POLICY_URL = "https://example.com/privacy"
private const val TERMS_URL = "https://example.com/terms"

class AppStore(
    private val repository: BirthdayRepository,
    private val familyRepository: FamilyProfileRepository,
    private val settingsRepository: AppSettingsRepository,
    private val eventHistoryRepository: EventHistoryRepository
) {
    private val eventEligibilityChecker = EventEligibilityChecker(eventHistoryRepository)
    private val eventHistoryManager = EventHistoryManager(eventHistoryRepository)
    private val eventDataBuilder = EventScreenDataBuilder(eventEligibilityChecker, eventHistoryManager)


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

        if (repository.isOnboardingCompleted()) {
            val activeProfile = getActiveProfileOrFallback()
            if (activeProfile != null) {
                val now = currentInstant()
                val birthData = activeProfile.toBirthdayData()
                val result = BillionSecondsCalculator.computeAll(birthData, now)
                val unknownTime = activeProfile.unknownBirthTime
                _state.value = AppState(
                    screen = AppScreen.Main(MainTab.Home),
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
                    countdown  = buildCountdownUiState(result, unknownTime, now),
                    lifeStats  = buildLifeStatsUiState(birthData, result, unknownTime, now),
                    milestones = buildMilestonesUiState(birthData, unknownTime, now),
                    family     = buildFamilyUiState(now),
                    profile    = buildProfileUiState(now)
                )
                startTick(result.milestoneInstant)
                // Проверка event eligibility при старте приложения
                checkEventEligibilityAndTrigger(activeProfile, now)
            }
        }
    }

    fun dispatch(intent: AppIntent) {
        _state.update { AppReducer.reduce(it, intent) }
        when (intent) {
            is AppIntent.OnboardingCalculateClicked -> onboardingCalculate()
            is AppIntent.OnboardingContinueClicked  -> onboardingContinue()
            is AppIntent.CalculateClicked           -> mainCalculate()
            is AppIntent.ClearClicked               -> mainClear()
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
                if (_state.value.screen is AppScreen.Main) {
                    val profileSubScreen = _state.value.profile.subScreen
                    if (profileSubScreen !is ProfileSubScreen.Root) {
                        // Есть открытый sub-screen Profile — возврат на Root
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
            }
            // Profile
            is AppIntent.ProfileScreenStarted       -> onProfileResumed()
            is AppIntent.ProfileScreenResumed       -> onProfileResumed()
            is AppIntent.ActiveProfileSummaryClicked -> emitEffect(AppEffect.NavigateToFamily)
            is AppIntent.PremiumClicked             -> emitEffect(AppEffect.ShowComingSoon("premium"))
            is AppIntent.TimeCapsuleClicked         -> emitEffect(AppEffect.ShowComingSoon("time_capsule"))
            is AppIntent.HelpClicked                -> emitEffect(AppEffect.ShowComingSoon("help"))
            is AppIntent.LegalLinkClicked           -> onLegalLinkClicked(intent.type)
            is AppIntent.NotificationsToggled,
            is AppIntent.MilestoneRemindersToggled,
            is AppIntent.FamilyRemindersToggled,
            is AppIntent.ReengagementToggled,
            is AppIntent.ApproximateLabelsToggled,
            is AppIntent.Use24HourFormatToggled     -> persistSettings()
            is AppIntent.ConfirmDangerousAction     -> onConfirmDangerousAction()
            // Debug
            is AppIntent.DebugOpenEventScreen       -> onDebugOpenEventScreen()
            // Event Screen
            is AppIntent.Event.ScreenOpened         -> onEventScreenOpened(intent.profileId, intent.source)
            is AppIntent.Event.MarkSeenIfNeeded     -> onEventMarkSeen()
            is AppIntent.Event.MarkCelebrationShownIfNeeded -> onEventMarkCelebrationShown()
            is AppIntent.Event.CelebrationCompleted -> onEventCelebrationCompleted()
            is AppIntent.Event.CelebrationSkipped   -> onEventMarkCelebrationShown()
            is AppIntent.Event.ShareClicked         -> onEventShareClicked()
            is AppIntent.Event.CreateVideoClicked   -> emitEffect(AppEffect.ShowComingSoon("create_video"))
            is AppIntent.Event.OpenMilestonesClicked -> emitEffect(AppEffect.NavigateToMilestonesFromEvent)
            is AppIntent.Event.OpenStatsClicked     -> emitEffect(AppEffect.NavigateToStatsFromEvent)
            is AppIntent.Event.GoHomeClicked        -> emitEffect(AppEffect.NavigateToHomeFromEvent)
            is AppIntent.Event.NextMilestoneClicked -> emitEffect(AppEffect.ShowComingSoon("next_milestone"))
            is AppIntent.Event.RetryClicked         -> onEventRetry()
            is AppIntent.Event.ProfileChanged       -> onEventProfileChanged(intent.newProfileId)
            is AppIntent.Event.BackPressed,
            is AppIntent.Event.DismissClicked       -> Unit // reducer handles navigation
            else -> Unit
        }
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
                    screen             = AppScreen.OnboardingResult,
                    error              = null
                )
            }
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
                screen         = AppScreen.Main(MainTab.Home),
                showMainResult = true,
                countdown    = buildCountdownUiState(result, s.unknownTime, now),
                lifeStats    = buildLifeStatsUiState(data, result, s.unknownTime, now),
                milestones   = buildMilestonesUiState(data, s.unknownTime, now),
                family       = buildFamilyUiState(now),
                profile      = buildProfileUiState(now)
            )
        }
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
            it.copy(countdown = buildCountdownUiState(result, activeProfile.unknownBirthTime, now))
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
                    countdown  = buildCountdownUiState(result, unknownTime, now),
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
                    countdown  = buildCountdownUiState(result, unknownTime, now),
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
                countdown  = buildCountdownUiState(result, unknownTime, now),
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
                screen = AppScreen.EventScreen(profile.id, EventSource.MANUAL),
                event  = it.event.copy(
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
        emitEffect(AppEffect.TriggerCelebrationAnimation)
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
                emitEffect(AppEffect.TriggerCelebrationAnimation)
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
                        countdown  = buildCountdownUiState(result, unknownTime, now),
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
        now: Instant
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
                ProfileConfirmDialog.ResetOnboarding,
                ProfileConfirmDialog.ClearAllData -> {
                    tickJob?.cancel()
                    repository.clearBirthday()
                    repository.setOnboardingCompleted(false)
                    familyRepository.clearAll()
                    settingsRepository.clearAll()
                    _state.value = AppState()
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
            error                = null
        )
    }

    private fun emitEffect(effect: AppEffect) {
        scope.launch { _effect.emit(effect) }
    }

    fun dispose() = scope.cancel()
}
