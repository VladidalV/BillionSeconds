package com.example.billionseconds.mvi

import com.example.billionseconds.data.repository.BirthdayRepository
import com.example.billionseconds.domain.BillionSecondsCalculator
import com.example.billionseconds.util.DateTimeFormatter
import com.example.billionseconds.util.localDateTimeToInstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat

class BirthdayStore(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
    private val repository: BirthdayRepository = BirthdayRepository()
) {
    private val _state = MutableStateFlow(BirthdayState())
    val state: StateFlow<BirthdayState> = _state.asStateFlow()

    private val calculator: BillionSecondsCalculator = BillionSecondsCalculator
    private val formatter: DateTimeFormatter = DateTimeFormatter

    init {
        loadSavedBirthday()
    }

    private fun loadSavedBirthday() {
        scope.launch {
            val savedData = repository.getBirthday()
            savedData?.let { data ->
                try {
                    val date = LocalDate.parse(data.birthDate)
                    val time = LocalTime.parse(data.birthTime)
                    updateState { currentState ->
                        currentState.copy(
                            birthDate = date,
                            birthTime = time,
                            isCalculateEnabled = true
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun handle(intent: BirthdayIntent) {
        when (intent) {
            BirthdayIntent.CalculateClicked -> calculateBillionSeconds()
            is BirthdayIntent.DateSelected -> {
                scope.launch {
                    try {
                        repository.saveBirthday(
                            birthDate = intent.date.toString(),
                            birthTime = _state.value.birthTime?.toString() ?: "12:00:00"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                updateState { BirthdayReducer.reduce(it, intent) }
            }
            is BirthdayIntent.TimeSelected -> {
                scope.launch {
                    try {
                        repository.saveBirthday(
                            birthDate = _state.value.birthDate?.toString() ?: "2000-01-01",
                            birthTime = intent.time.toString()
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                updateState { BirthdayReducer.reduce(it, intent) }
            }
            else -> updateState { BirthdayReducer.reduce(it, intent) }
        }
    }

    private fun calculateBillionSeconds() {
        updateState { BirthdayReducer.reduce(it, BirthdayIntent.CalculateClicked) }
        
        val currentState = _state.value
        if (currentState.birthDate != null && currentState.birthTime != null) {
            scope.launch {
                try {
                    val billionSecondsInstant = calculator.calculateBillionSeconds(
                        birthDate = currentState.birthDate,
                        birthTime = currentState.birthTime
                    )
                    
                    val birthInstant = localDateTimeToInstant(
                        date = currentState.birthDate,
                        time = currentState.birthTime,
                        timeZone = TimeZone.currentSystemDefault()
                    )

                    val resultText = formatter.formatDetailedResult(
                        billionSecondsInstant = billionSecondsInstant,
                        birthInstant = birthInstant
                    )
                    
                    updateState { currentState ->
                        currentState.copy(
                            billionSecondsInstant = billionSecondsInstant,
                            resultText = resultText,
                            isLoading = false
                        )
                    }
                } catch (e: Exception) {
                    updateState { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = "Calculation failed: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    private fun updateState(update: (BirthdayState) -> BirthdayState) {
        _state.value = update(_state.value)
    }

    fun dispose() {
        (scope as? Job)?.cancel()
    }
}