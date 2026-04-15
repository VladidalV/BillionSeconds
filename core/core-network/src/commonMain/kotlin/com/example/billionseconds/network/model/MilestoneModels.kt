package com.example.billionseconds.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MilestoneProgressResponse(
    @SerialName("profile_id")             val profileId: String,
    @SerialName("last_seen_reached_id")   val lastSeenReachedId: String?
)

@Serializable
data class MilestoneProgressRequest(
    @SerialName("last_seen_reached_id") val lastSeenReachedId: String
)
