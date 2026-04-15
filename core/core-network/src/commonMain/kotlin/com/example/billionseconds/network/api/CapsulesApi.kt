package com.example.billionseconds.network.api

import com.example.billionseconds.network.NetworkConfig.API_PREFIX
import com.example.billionseconds.network.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class CapsulesApi(private val client: HttpClient) {

    suspend fun getCapsules(): CapsulesResponse =
        client.get("$API_PREFIX/capsules").body()

    suspend fun createCapsule(request: CapsuleRequest): CapsuleResponse =
        client.post("$API_PREFIX/capsules") { setBody(request) }.body()

    suspend fun updateCapsule(capsuleId: String, request: CapsuleRequest): CapsuleResponse =
        client.put("$API_PREFIX/capsules/$capsuleId") { setBody(request) }.body()

    suspend fun deleteCapsule(capsuleId: String) {
        client.delete("$API_PREFIX/capsules/$capsuleId")
    }

    suspend fun openCapsule(capsuleId: String): CapsuleResponse =
        client.post("$API_PREFIX/capsules/$capsuleId/open").body()
}
