package com.example.billionseconds.network.client

import com.example.billionseconds.network.NetworkConfig
import com.example.billionseconds.network.model.AuthResponse
import com.example.billionseconds.network.model.RefreshTokenRequest
import com.example.billionseconds.network.token.TokenManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createHttpClient(tokenManager: TokenManager): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }

        install(Logging) {
            level = LogLevel.BODY
            logger = Logger.DEFAULT
        }

        install(HttpTimeout) {
            connectTimeoutMillis = NetworkConfig.CONNECT_TIMEOUT_MS
            requestTimeoutMillis = NetworkConfig.REQUEST_TIMEOUT_MS
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val access = tokenManager.getAccessToken() ?: return@loadTokens null
                    val refresh = tokenManager.getRefreshToken() ?: return@loadTokens null
                    BearerTokens(access, refresh)
                }
                refreshTokens {
                    val newTokens = tokenManager.withRefreshLock {
                        // Re-check: another coroutine may have already refreshed
                        val currentAccess = tokenManager.getAccessToken()
                        if (currentAccess != null && currentAccess != oldTokens?.accessToken) {
                            // Already refreshed by another coroutine
                            return@withRefreshLock AuthResponse(
                                accessToken  = currentAccess,
                                refreshToken = tokenManager.getRefreshToken() ?: "",
                                userId       = tokenManager.getUserId() ?: ""
                            )
                        }
                        val refreshToken = tokenManager.getRefreshToken() ?: return@withRefreshLock null
                        try {
                            client.post("${NetworkConfig.BASE_URL}${NetworkConfig.API_PREFIX}/auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(RefreshTokenRequest(refreshToken))
                                markAsRefreshTokenRequest()
                            }.body<AuthResponse>()
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (newTokens != null) {
                        tokenManager.saveTokens(newTokens)
                        BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                    } else {
                        tokenManager.clearTokens()
                        null
                    }
                }
                sendWithoutRequest { request ->
                    request.url.host == Url(NetworkConfig.BASE_URL).host
                }
            }
        }

        defaultRequest {
            url(NetworkConfig.BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }
}
