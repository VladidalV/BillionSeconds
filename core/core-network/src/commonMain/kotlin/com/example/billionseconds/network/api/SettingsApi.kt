package com.example.billionseconds.network.api

import com.example.billionseconds.network.NetworkConfig.API_PREFIX
import com.example.billionseconds.network.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class SettingsApi(private val client: HttpClient) {

    suspend fun getSettings(): SettingsResponse =
        client.get("$API_PREFIX/settings").body()

    suspend fun patchSettings(request: SettingsPatchRequest): SettingsResponse =
        client.patch("$API_PREFIX/settings") { setBody(request) }.body()
}
