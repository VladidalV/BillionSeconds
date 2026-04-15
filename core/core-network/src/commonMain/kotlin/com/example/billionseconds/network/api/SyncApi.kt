package com.example.billionseconds.network.api

import com.example.billionseconds.network.NetworkConfig.API_PREFIX
import com.example.billionseconds.network.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class SyncApi(private val client: HttpClient) {

    suspend fun getSync(): SyncResponse =
        client.get("$API_PREFIX/sync").body()

    /** POST /api/v1/migrate — uploads local data to the server on first launch. */
    suspend fun migrateData(request: MigrateRequest): MigrateResponse =
        client.post("$API_PREFIX/migrate") { setBody(request) }.body()
}
