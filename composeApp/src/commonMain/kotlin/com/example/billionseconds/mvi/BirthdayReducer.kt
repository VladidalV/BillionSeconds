package com.example.billionseconds.mvi

object BirthdayReducer {
    fun reduce(state: BirthdayState, intent: BirthdayIntent): BirthdayState = when (intent) {
        is BirthdayIntent.DateChanged -> state.copy(
            year = intent.year,
            month = intent.month,
            day = intent.day,
            error = null
        )
        is BirthdayIntent.TimeChanged -> state.copy(
            hour = intent.hour,
            minute = intent.minute
        )
        is BirthdayIntent.CalculateClicked -> state.copy(error = null)
        is BirthdayIntent.ClearClicked -> BirthdayState()
    }
}
