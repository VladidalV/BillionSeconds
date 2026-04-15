package com.example.billionseconds.network.api

import com.example.billionseconds.network.NetworkConfig.API_PREFIX
import com.example.billionseconds.network.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class AuthApi(private val client: HttpClient) {

    suspend fun loginAnonymous(request: AnonymousAuthRequest): AuthResponse =
        client.post("$API_PREFIX/auth/anonymous") { setBody(request) }.body()

    suspend fun loginWithApple(request: AppleAuthRequest): AuthResponse =
        client.post("$API_PREFIX/auth/apple") { setBody(request) }.body()

    suspend fun loginWithGoogle(request: GoogleAuthRequest): AuthResponse =
        client.post("$API_PREFIX/auth/google") { setBody(request) }.body()

    suspend fun refreshToken(request: RefreshTokenRequest): AuthResponse =
        client.post("$API_PREFIX/auth/refresh") { setBody(request) }.body()

    suspend fun logout() {
        client.post("$API_PREFIX/auth/logout")
    }

    suspend fun mergeAccount(request: MergeAccountRequest): AuthResponse =
        client.post("$API_PREFIX/auth/merge") { setBody(request) }.body()

    suspend fun updateDevice(request: UpdateDeviceRequest) {
        client.put("$API_PREFIX/me/device") { setBody(request) }
    }
}
