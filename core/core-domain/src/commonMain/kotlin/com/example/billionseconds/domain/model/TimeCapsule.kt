package com.example.billionseconds.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimeCapsule(
    val id: String,
    val title: String,
    val message: String,
    val recipientProfileId: String? = null,     // null = себе
    val unlockCondition: UnlockCondition,
    val createdAt: Long,                         // epochMillis
    val openedAt: Long? = null,
    val isDraft: Boolean = false
)

@Serializable
sealed class UnlockCondition {
    @Serializable
    @SerialName("exact_date_time")
    data class ExactDateTime(val epochMillis: Long) : UnlockCondition()

    @Serializable
    @SerialName("billion_seconds_event")
    data class BillionSecondsEvent(val profileId: String) : UnlockCondition()
}
