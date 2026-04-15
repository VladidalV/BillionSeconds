package com.example.billionseconds.network.api

import com.example.billionseconds.network.NetworkConfig.API_PREFIX
import com.example.billionseconds.network.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class ProfilesApi(private val client: HttpClient) {

    suspend fun getProfiles(): ProfilesResponse =
        client.get("$API_PREFIX/profiles").body()

    suspend fun createProfile(request: ProfileRequest): ProfileResponse =
        client.post("$API_PREFIX/profiles") { setBody(request) }.body()

    suspend fun updateProfile(profileId: String, request: ProfileRequest): ProfileResponse =
        client.put("$API_PREFIX/profiles/$profileId") { setBody(request) }.body()

    suspend fun deleteProfile(profileId: String) {
        client.delete("$API_PREFIX/profiles/$profileId")
    }

    /** PUT /api/v1/profiles/active — sets the active profile for the current user. */
    suspend fun setActiveProfile(request: SetActiveProfileRequest) {
        client.put("$API_PREFIX/profiles/active") { setBody(request) }
    }
}
