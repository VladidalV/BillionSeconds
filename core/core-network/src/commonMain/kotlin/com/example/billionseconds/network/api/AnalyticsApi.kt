package com.example.billionseconds.network.api

import com.example.billionseconds.network.NetworkConfig.API_PREFIX
import com.example.billionseconds.network.model.*
import io.ktor.client.*
import io.ktor.client.request.*

class AnalyticsApi(private val client: HttpClient) {

    suspend fun sendEvents(request: AnalyticsBatchRequest) {
        client.post("$API_PREFIX/analytics/events") { setBody(request) }
    }
}
