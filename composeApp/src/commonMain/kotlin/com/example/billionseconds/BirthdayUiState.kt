package com.example.billionseconds

import com.example.billionseconds.util.getCurrentInstant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

public data class BirthdayUiState(
    public val birthDate: LocalDate? = null,
    public val birthTime: LocalTime? = null,
    public val billionSecondsInstant: kotlinx.datetime.Instant? = null,
    public val resultText: String = "",
    public val isCalculateEnabled: Boolean = false,
    public val isLoading: Boolean = false,
    public val errorMessage: String? = null
) {
    public val hasResult: Boolean
        get() = billionSecondsInstant != null && resultText.isNotEmpty()

    public val isBillionSecondsReached: Boolean
        get() = billionSecondsInstant?.let { 
            val currentTime = getCurrentInstant()
            currentTime >= billionSecondsInstant
        } ?: false

    public val formattedBillionSecondsDate: String
        get() = billionSecondsInstant?.let { instant ->
            val timeZone = TimeZone.currentSystemDefault()
            val localDateTime = instant.toLocalDateTime(timeZone)
            localDateTime.toString()
        } ?: ""

    public companion object {
        public fun initial(): BirthdayUiState = BirthdayUiState()
    }
}
