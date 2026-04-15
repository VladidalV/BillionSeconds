package com.example.billionseconds.network.api

import com.example.billionseconds.network.NetworkConfig.API_PREFIX
import com.example.billionseconds.network.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class EventHistoryApi(private val client: HttpClient) {

    suspend fun getAllEventHistory(): EventHistoryListResponse =
        client.get("$API_PREFIX/event-history").body()

    suspend fun getEventHistory(profileId: String): EventHistoryResponse =
        client.get("$API_PREFIX/event-history/$profileId").body()

    suspend fun patchEventHistory(
        profileId: String,
        request: EventHistoryPatchRequest
    ): EventHistoryResponse =
        client.patch("$API_PREFIX/event-history/$profileId") { setBody(request) }.body()
}
