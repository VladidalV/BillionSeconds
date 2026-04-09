package com.example.billionseconds.mvi

import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.model.BirthdayData
import com.example.billionseconds.domain.BillionSecondsCalculator
import com.example.billionseconds.domain.BirthdayValidator
import com.example.billionseconds.domain.CountdownFormatter
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
                countdown = buildCountdownUiState(result, unknownTime, now)
            )
            startTick(result.milestoneInstant)
        }
        // else: остаётся AppScreen.OnboardingIntro (default)
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
            is AppIntent.ShareClicked               -> onShare()
            is AppIntent.CreateVideoClicked         -> emitEffect(AppEffect.ShowComingSoon("create_video"))
            is AppIntent.WriteLetterClicked         -> emitEffect(AppEffect.ShowComingSoon("write_letter"))
            is AppIntent.AddFamilyClicked           -> emitEffect(AppEffect.ShowComingSoon("add_family"))
            is AppIntent.BackClicked                -> {
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
                    milestoneInstant = result.milestoneInstant,
                    progressPercent = result.progressPercent,
                    isMilestoneReached = result.isMilestoneReached,
                    secondsRemaining = result.secondsRemaining,
                    screen = AppScreen.OnboardingResult,
                    error = null
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
        val result = BillionSecondsCalculator.computeAll(
            BirthdayData(s.year!!, s.month!!, s.day!!, s.hour, s.minute), now
        )
        _state.update {
            it.copy(
                screen = AppScreen.Main(MainTab.Home),
                showMainResult = true,
                countdown = buildCountdownUiState(result, s.unknownTime, now)
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
            val now = currentInstant()
            val result = BillionSecondsCalculator.computeAll(data, now)
            repository.saveBirthday(data)
            _state.update {
                it.copy(
                    milestoneInstant = result.milestoneInstant,
                    progressPercent = result.progressPercent,
                    isMilestoneReached = result.isMilestoneReached,
                    secondsRemaining = result.secondsRemaining,
                    showMainResult = true,
                    error = null,
                    countdown = buildCountdownUiState(result, s.unknownTime, now)
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
        // clearBirthday() сбрасывает все ключи включая onboarding_completed.
        // Восстанавливаем флаг — онбординг уже пройден.
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
                    _state.update {
                        it.copy(
                            isMilestoneReached = result.isMilestoneReached,
                            secondsRemaining = result.secondsRemaining,
                            countdown = buildCountdownUiState(result, s.unknownTime, now)
                        )
                    }
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildCountdownUiState(
        result: MilestoneResult,
        unknownTime: Boolean,
        now: Instant
    ): CountdownUiState = CountdownUiState(
        isLoading = false,
        eventStatus = result.toEventStatus(now),
        milestoneInstant = result.milestoneInstant,
        progressFraction = result.progressPercent,
        secondsRemaining = result.secondsRemaining,
        isUnknownBirthTime = unknownTime,
        formattedMilestoneDate = CountdownFormatter.formatMilestoneDate(result.milestoneInstant),
        formattedMilestoneTime = CountdownFormatter.formatMilestoneTime(result.milestoneInstant),
        formattedCountdown = CountdownFormatter.formatCountdown(result.secondsRemaining),
        formattedProgress = CountdownFormatter.formatProgress(result.progressPercent),
        error = null
    )

    private fun emitEffect(effect: AppEffect) {
        scope.launch { _effect.emit(effect) }
    }

    fun dispose() = scope.cancel()
}
