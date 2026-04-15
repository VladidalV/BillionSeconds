package com.example.billionseconds.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventHistoryResponse(
    @SerialName("profile_id")            val profileId: String,
    @SerialName("first_shown_at")        val firstShownAt: String?,
    @SerialName("celebration_shown_at")  val celebrationShownAt: String?,
    @SerialName("share_prompt_shown_at") val sharePromptShownAt: String?,
    @SerialName("updated_at")            val updatedAt: String
)

@Serializable
data class EventHistoryListResponse(
    val records: List<EventHistoryResponse>
)

@Serializable
data class EventHistoryPatchRequest(
    @SerialName("first_shown_at")        val firstShownAt: String? = null,
    @SerialName("celebration_shown_at")  val celebrationShownAt: String? = null,
    @SerialName("share_prompt_shown_at") val sharePromptShownAt: String? = null
)
