package com.example.billionseconds.mvi

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

sealed interface BirthdayIntent {
    
    data class DateSelected(val date: LocalDate) : BirthdayIntent
    
    data class TimeSelected(val time: LocalTime) : BirthdayIntent
    
    data object CalculateClicked : BirthdayIntent
    
    data object ClearResult : BirthdayIntent
}