package com.example.billionseconds.mvi

import com.example.billionseconds.util.getCurrentInstant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class BirthdayState(
    val birthDate: LocalDate? = null,
    val birthTime: LocalTime? = null,
    val billionSecondsInstant: kotlinx.datetime.Instant? = null,
    val resultText: String = "",
    val isCalculateEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val hasResult: Boolean
        get() = billionSecondsInstant != null && resultText.isNotEmpty()
    
    val isBillionSecondsReached: Boolean
        get() = billionSecondsInstant?.let { 
            val currentTime = getCurrentInstant()
            currentTime >= billionSecondsInstant
        } ?: false
    
    val formattedBillionSecondsDate: String
        get() = billionSecondsInstant?.let { instant ->
            val timeZone = TimeZone.currentSystemDefault()
            val localDateTime = instant.toLocalDateTime(timeZone)
            localDateTime.toString()
        } ?: ""
}