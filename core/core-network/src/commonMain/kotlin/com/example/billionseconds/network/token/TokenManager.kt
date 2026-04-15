package com.example.billionseconds.network.token

import com.example.billionseconds.network.model.AuthResponse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TokenManager(private val storage: TokenStorage) {

    private val refreshMutex = Mutex()

    fun getAccessToken(): String? = storage.getAccessToken()
    fun getRefreshToken(): String? = storage.getRefreshToken()
    fun getUserId(): String? = storage.getUserId()
    fun getDeviceId(): String = storage.getOrCreateDeviceId()
    fun isAuthenticated(): Boolean = storage.getAccessToken() != null

    fun saveTokens(response: AuthResponse) {
        storage.setAccessToken(response.accessToken)
        storage.setRefreshToken(response.refreshToken)
        storage.setUserId(response.userId)
    }

    fun clearTokens() {
        storage.clear()
    }

    /** Ensures only one coroutine performs a token refresh at a time. */
    suspend fun <T> withRefreshLock(block: suspend () -> T): T = refreshMutex.withLock { block() }
}
