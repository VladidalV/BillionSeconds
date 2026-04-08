package com.example.billionseconds.mvi

import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.model.BirthdayData
import com.example.billionseconds.domain.BillionSecondsCalculator
import com.example.billionseconds.domain.BirthdayValidator
import com.example.billionseconds.navigation.AppScreen
import com.example.billionseconds.util.currentInstant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant

class AppStore(private val repository: BirthdayRepository) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

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
            _state.value = AppState(
                screen = AppScreen.Main,
                year = saved.year,
                month = saved.month,
                day = saved.day,
                hour = saved.hour,
                minute = saved.minute,
                milestoneInstant = result.milestoneInstant,
                progressPercent = result.progressPercent,
                isMilestoneReached = result.isMilestoneReached,
                secondsRemaining = result.secondsRemaining,
                showMainResult = true
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
            else -> Unit
        }
    }

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
        val milestone = _state.value.milestoneInstant ?: return
        _state.update { it.copy(screen = AppScreen.Main, showMainResult = true) }
        startTick(milestone)
    }

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
                    error = null
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
        // clearBirthday() вызывает storage.clear() который сбрасывает все ключи,
        // включая onboarding_completed. Восстанавливаем флаг — онбординг уже пройден.
        repository.setOnboardingCompleted(true)
    }

    private fun startTick(milestone: Instant) {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (true) {
                delay(1000)
                val now = currentInstant()
                _state.update {
                    it.copy(
                        isMilestoneReached = BillionSecondsCalculator.isReached(milestone, now),
                        secondsRemaining = maxOf(0L, BillionSecondsCalculator.secondsUntil(milestone, now))
                    )
                }
            }
        }
    }

    fun dispose() = scope.cancel()
}
