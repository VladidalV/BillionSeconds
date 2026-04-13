package com.example.billionseconds.data.event

import kotlinx.serialization.Serializable

@Serializable
data class EventHistoryRecord(
    val profileId: String,
    val firstShownAt: Long,           // epochSeconds — момент первого открытия экрана
    val celebrationShownAt: Long? = null,   // epochSeconds, null если ещё не показывалась
    val sharePromptShownAt: Long? = null    // epochSeconds, null если ещё не показывался
)
