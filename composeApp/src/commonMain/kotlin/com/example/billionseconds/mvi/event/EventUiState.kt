package com.example.billionseconds.mvi.event

import com.example.billionseconds.domain.event.model.EventMode
import com.example.billionseconds.domain.event.model.EventSource
import kotlinx.datetime.Instant

enum class EventScreenStatus {
    Loading,
    FirstTime,
    Repeat,
    NotEligible,
    ProfileMissing,
    Error
}

data class EventUiState(
    val isLoading: Boolean = true,
    // Profile
    val profileId: String = "",
    val profileName: String = "",
    // Event
    val targetDateTime: Instant? = null,
    val firstShownAt: Instant? = null,
    val isApproximateMode: Boolean = false,
    // Режим и источник
    val mode: EventMode? = null,
    val source: EventSource = EventSource.MANUAL,
    // Экранные флаги
    val screenStatus: EventScreenStatus = EventScreenStatus.Loading,
    val isBackAllowed: Boolean = false,
    val isCelebrationRunning: Boolean = false,
    val celebrationCompleted: Boolean = false,
    // Сессионный флаг — не персистентный, сбрасывается при смене профиля
    val autoOpenTriggered: Boolean = false,
    // UI model (готовые строки и списки для отрисовки)
    val uiModel: EventUiModel? = null,
    // Ошибка
    val errorMessage: String? = null
)
