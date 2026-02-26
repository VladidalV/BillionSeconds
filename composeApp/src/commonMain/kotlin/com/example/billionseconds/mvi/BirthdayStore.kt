package com.example.billionseconds.mvi

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
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

class BirthdayStore(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())
) {
    private val _state = MutableStateFlow(BirthdayState())
    val state: StateFlow<BirthdayState> = _state.asStateFlow()

    private val calculator: BillionSecondsCalculator = BillionSecondsCalculator
    private val formatter: DateTimeFormatter = DateTimeFormatter

    fun handle(intent: BirthdayIntent) {
        when (intent) {
            BirthdayIntent.CalculateClicked -> calculateBillionSeconds()
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