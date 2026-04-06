package com.example.billionseconds.mvi

import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.model.BirthdayData
import com.example.billionseconds.domain.BillionSecondsCalculator
import com.example.billionseconds.util.currentInstant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant

class BirthdayStore(private val repository: BirthdayRepository) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _state = MutableStateFlow(BirthdayState())
    val state: StateFlow<BirthdayState> = _state.asStateFlow()

    private var tickJob: Job? = null

    init {
        repository.getBirthday()?.let { saved ->
            val milestone = BillionSecondsCalculator.calculateMilestone(saved)
            val now = currentInstant()
            _state.value = BirthdayState(
                year = saved.year,
                month = saved.month,
                day = saved.day,
                hour = saved.hour,
                minute = saved.minute,
                milestoneInstant = milestone,
                isMilestoneReached = BillionSecondsCalculator.isReached(milestone, now),
                secondsRemaining = maxOf(0L, BillionSecondsCalculator.secondsUntil(milestone, now)),
                showResult = true
            )
            startTick(milestone)
        }
    }

    fun dispatch(intent: BirthdayIntent) {
        _state.update { BirthdayReducer.reduce(it, intent) }
        when (intent) {
            is BirthdayIntent.CalculateClicked -> calculate()
            is BirthdayIntent.ClearClicked -> {
                tickJob?.cancel()
                repository.clearBirthday()
            }
            else -> Unit
        }
    }

    private fun calculate() {
        val s = _state.value
        val year = s.year ?: run { _state.update { it.copy(error = "Введите дату") }; return }
        val month = s.month ?: run { _state.update { it.copy(error = "Введите дату") }; return }
        val day = s.day ?: run { _state.update { it.copy(error = "Введите дату") }; return }

        try {
            val data = BirthdayData(year, month, day, s.hour, s.minute)
            val milestone = BillionSecondsCalculator.calculateMilestone(data)
            val now = currentInstant()
            repository.saveBirthday(data)
            _state.update {
                it.copy(
                    milestoneInstant = milestone,
                    isMilestoneReached = BillionSecondsCalculator.isReached(milestone, now),
                    secondsRemaining = maxOf(0L, BillionSecondsCalculator.secondsUntil(milestone, now)),
                    showResult = true,
                    error = null
                )
            }
            startTick(milestone)
        } catch (e: Exception) {
            _state.update { it.copy(error = "Некорректная дата") }
        }
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
