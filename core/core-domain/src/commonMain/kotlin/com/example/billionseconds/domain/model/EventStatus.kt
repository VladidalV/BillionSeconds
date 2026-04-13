package com.example.billionseconds.domain.model

import kotlinx.datetime.*

sealed class EventStatus {
    data object Upcoming : EventStatus()
    data object Today    : EventStatus()
    data object Reached  : EventStatus()
}

fun MilestoneResult.toEventStatus(now: Instant): EventStatus {
    if (isMilestoneReached) return EventStatus.Reached
    val milestoneDate = milestoneInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return if (milestoneDate == today) EventStatus.Today else EventStatus.Upcoming
}
