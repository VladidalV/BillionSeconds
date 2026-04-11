package com.example.billionseconds.domain.event.model

sealed class EventEligibilityStatus {
    data object NotReached : EventEligibilityStatus()
    data object EligibleFirstTime : EventEligibilityStatus()
    data object EligibleRepeat : EventEligibilityStatus()
    data class Error(val reason: String) : EventEligibilityStatus()
}
