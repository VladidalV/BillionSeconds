package com.example.billionseconds.mvi

sealed class BirthdayIntent {
    data class DateChanged(val year: Int, val month: Int, val day: Int) : BirthdayIntent()
    data class TimeChanged(val hour: Int, val minute: Int) : BirthdayIntent()
    data object CalculateClicked : BirthdayIntent()
    data object ClearClicked : BirthdayIntent()
}
