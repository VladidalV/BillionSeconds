package com.example.billionseconds.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CapsuleRequest(
    val title: String,
    val message: String,
    @SerialName("recipient_profile_id")  val recipientProfileId: String? = null,
    @SerialName("is_draft")              val isDraft: Boolean = false,
    @SerialName("unlock_condition_type") val unlockConditionType: String,
    @SerialName("unlock_at_epoch_ms")    val unlockAtEpochMs: Long? = null,
    @SerialName("unlock_profile_id")     val unlockProfileId: String? = null
)

@Serializable
data class CapsuleResponse(
    val id: String,
    val title: String,
    val message: String,
    @SerialName("recipient_profile_id")  val recipientProfileId: String?,
    @SerialName("unlock_condition_type") val unlockConditionType: String,
    @SerialName("unlock_at_epoch_ms")    val unlockAtEpochMs: Long?,
    @SerialName("unlock_profile_id")     val unlockProfileId: String?,
    @SerialName("is_draft")              val isDraft: Boolean,
    @SerialName("opened_at")             val openedAt: String?,
    @SerialName("created_at")            val createdAt: String,
    @SerialName("updated_at")            val updatedAt: String
)

@Serializable
data class CapsulesResponse(
    val capsules: List<CapsuleResponse>
)
