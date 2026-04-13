package com.example.billionseconds.ui.countdown

import com.example.billionseconds.domain.model.EventStatus
import kotlinx.datetime.Instant

data class CountdownUiState(
    val isLoading: Boolean          = true,
    val eventStatus: EventStatus    = EventStatus.Upcoming,

    val milestoneInstant: Instant?  = null,
    val progressFraction: Float     = 0f,
    val secondsRemaining: Long      = 0L,
    val isUnknownBirthTime: Boolean = false,

    val formattedMilestoneDate: String = "",
    val formattedMilestoneTime: String = "",
    val formattedCountdown: String     = "",
    val formattedProgress: String      = "",

    val activeProfileName: String?  = null,
    val error: CountdownError?      = null
)

sealed class CountdownError {
    data object NoProfileData : CountdownError()
    data object CorruptedData : CountdownError()
}
