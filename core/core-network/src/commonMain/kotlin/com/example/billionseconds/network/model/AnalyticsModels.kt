package com.example.billionseconds.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsEventItem(
    @SerialName("event_type")  val eventType: String,
    @SerialName("occurred_at") val occurredAt: String,
    val properties: Map<String, String> = emptyMap()
)

@Serializable
data class AnalyticsBatchRequest(
    val events: List<AnalyticsEventItem>
)
