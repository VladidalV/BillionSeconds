package com.example.billionseconds.mvi

import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.model.BirthdayData
import com.example.billionseconds.domain.BillionSecondsCalculator
import com.example.billionseconds.domain.BirthdayValidator
import com.example.billionseconds.domain.CountdownFormatter
import com.example.billionseconds.domain.LifeStatsCalculator
import com.example.billionseconds.domain.LifeStatsFormatter
import com.example.billionseconds.domain.MilestoneConfig
import com.example.billionseconds.domain.MilestoneStatus
import com.example.billionseconds.domain.MilestonesCalculator
import com.example.billionseconds.domain.MilestonesFormatter
import com.example.billionseconds.domain.model.MilestoneResult
import com.example.billionseconds.domain.model.toEventStatus
import com.example.billionseconds.navigation.AppScreen
import com.example.billionseconds.navigation.MainTab
import com.example.billionseconds.util.currentInstant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant

class AppStore(private val repository: BirthdayRepository) {

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

        if (repository.isOnboardingCompleted() && saved != null) {
            val now = currentInstant()
            val result = BillionSecondsCalculator.computeAll(saved, now)
            val unknownTime = repository.isUnknownTime()
            _state.value = AppState(
                screen = AppScreen.Main(MainTab.Home),
                year = saved.year,
                month = saved.month,
                day = saved.day,
                hour = saved.hour,
                minute = saved.minute,
                unknownTime = unknownTime,
                milestoneInstant = result.milestoneInstant,
                progressPercent = result.progressPercent,
                isMilestoneReached = result.isMilestoneReached,
                secondsRemaining = result.secondsRemaining,
                showMainResult = true,
                countdown  = buildCountdownUiState(result, unknownTime, now),
                lifeStats  = buildLifeStatsUiState(saved, result, unknownTime, now),
                milestones = buildMilestonesUiState(saved, unknownTime, now)
            )
            startTick(result.milestoneInstant)
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
            is AppIntent.ShareClicked               -> onShare()
            is AppIntent.CreateVideoClicked         -> emitEffect(AppEffect.ShowComingSoon("create_video"))
            is AppIntent.WriteLetterClicked         -> emitEffect(AppEffect.ShowComingSoon("write_letter"))
            is AppIntent.AddFamilyClicked           -> emitEffect(AppEffect.ShowComingSoon("add_family"))
            is AppIntent.BackClicked -> {
                if (_state.value.screen is AppScreen.Main) emitEffect(AppEffect.ExitApp)
            }
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
        val result = BillionSecondsCalculator.computeAll(data, now)
        _state.update {
            it.copy(
                screen       = AppScreen.Main(MainTab.Home),
                showMainResult = true,
                countdown    = buildCountdownUiState(result, s.unknownTime, now),
                lifeStats    = buildLifeStatsUiState(data, result, s.unknownTime, now),
                milestones   = buildMilestonesUiState(data, s.unknownTime, now)
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
        val s = _state.value
        val saved = repository.getBirthday() ?: return
        val now = currentInstant()
        val result = BillionSecondsCalculator.computeAll(saved, now)
        _state.update {
            it.copy(countdown = buildCountdownUiState(result, s.unknownTime, now))
        }
    }

    // ── Life Stats screen ─────────────────────────────────────────────────────

    private fun onLifeStatsResumed() {
        val s = _state.value
        val saved = repository.getBirthday() ?: run {
            _state.update { it.copy(lifeStats = LifeStatsUiState(isLoading = false, error = LifeStatsError.NoBirthData)) }
            return
        }
        val now = currentInstant()
        val result = BillionSecondsCalculator.computeAll(saved, now)
        _state.update {
            it.copy(lifeStats = buildLifeStatsUiState(saved, result, s.unknownTime, now))
        }
    }

    // ── Milestones screen ─────────────────────────────────────────────────────

    private fun onMilestonesResumed() {
        val s = _state.value
        val saved = repository.getBirthday() ?: run {
            _state.update {
                it.copy(milestones = MilestonesUiState(isLoading = false, error = MilestonesError.NoBirthData))
            }
            return
        }
        val now = currentInstant()
        val newState = buildMilestonesUiState(saved, s.unknownTime, now)
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

    // ── Tick ─────────────────────────────────────────────────────────────────

    private fun startTick(milestone: Instant) {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (true) {
                delay(1000)
                val now = currentInstant()
                val s = _state.value
                val saved = repository.getBirthday()
                if (saved != null) {
                    val result = BillionSecondsCalculator.computeAll(saved, now)
                    val newMilestones = buildMilestonesUiState(saved, s.unknownTime, now)
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
                            countdown  = buildCountdownUiState(result, s.unknownTime, now),
                            lifeStats  = buildLifeStatsUiState(saved, result, s.unknownTime, now),
                            milestones = if (newlyReachedId != null)
                                newMilestones.copy(celebrationAvailableId = null)
                            else newMilestones
                        )
                    }
                }
            }
        }
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

    private fun emitEffect(effect: AppEffect) {
        scope.launch { _effect.emit(effect) }
    }

    fun dispose() = scope.cancel()
}
