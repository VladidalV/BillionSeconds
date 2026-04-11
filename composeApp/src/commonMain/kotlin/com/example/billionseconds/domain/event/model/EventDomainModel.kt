package com.example.billionseconds.domain.event.model

import com.example.billionseconds.data.model.RelationType
import kotlinx.datetime.Instant

data class EventDomainModel(
    // Из профиля
    val profileId: String,
    val profileName: String,
    val relationType: RelationType,
    val unknownBirthTime: Boolean,
    // Из BillionSecondsCalculator
    val targetDateTime: Instant,
    val isReached: Boolean,
    // Из EventHistoryRecord
    val wasShown: Boolean,
    val celebrationShown: Boolean,
    val sharePromptShown: Boolean,
    val firstShownAt: Instant?,
    // Вычислено в EventEligibilityChecker
    val mode: EventMode,
    val eligibilityStatus: EventEligibilityStatus,
    // Источник открытия
    val source: EventSource,
    // Приблизительный режим
    val isApproximateMode: Boolean
)
