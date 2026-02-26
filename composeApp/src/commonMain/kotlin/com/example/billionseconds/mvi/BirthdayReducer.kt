package com.example.billionseconds.mvi

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

object BirthdayReducer {

    fun reduce(state: BirthdayState, intent: BirthdayIntent): BirthdayState {
        return when (intent) {
            is BirthdayIntent.DateSelected -> handleDateSelected(state, intent.date)
            is BirthdayIntent.TimeSelected -> handleTimeSelected(state, intent.time)
            BirthdayIntent.CalculateClicked -> handleCalculateClicked(state)
            BirthdayIntent.ClearResult -> handleClearResult(state)
        }
    }

    private fun handleDateSelected(state: BirthdayState, date: LocalDate): BirthdayState {
        return state.copy(
            birthDate = date,
            isCalculateEnabled = state.birthTime != null,
            errorMessage = null
        )
    }

    private fun handleTimeSelected(state: BirthdayState, time: LocalTime): BirthdayState {
        return state.copy(
            birthTime = time,
            isCalculateEnabled = state.birthDate != null,
            errorMessage = null
        )
    }

    private fun handleCalculateClicked(state: BirthdayState): BirthdayState {
        return if (state.birthDate == null || state.birthTime == null) {
            state.copy(
                errorMessage = "Please select both date and time"
            )
        } else {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }
    }

    private fun handleClearResult(state: BirthdayState): BirthdayState {
        return state.copy(
            birthDate = null,
            birthTime = null,
            billionSecondsInstant = null,
            resultText = "",
            isCalculateEnabled = false,
            isLoading = false,
            errorMessage = null
        )
    }
}