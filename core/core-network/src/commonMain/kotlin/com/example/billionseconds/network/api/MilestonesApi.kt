package com.example.billionseconds.network.api

import com.example.billionseconds.network.NetworkConfig.API_PREFIX
import com.example.billionseconds.network.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class MilestonesApi(private val client: HttpClient) {

    suspend fun getMilestoneProgress(profileId: String): MilestoneProgressResponse =
        client.get("$API_PREFIX/profiles/$profileId/milestone-progress").body()

    /** PUT returns 204 — no response body. */
    suspend fun updateMilestoneProgress(
        profileId: String,
        request: MilestoneProgressRequest
    ) {
        client.put("$API_PREFIX/profiles/$profileId/milestone-progress") { setBody(request) }
    }
}
