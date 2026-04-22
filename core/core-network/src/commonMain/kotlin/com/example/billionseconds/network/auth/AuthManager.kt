package com.example.billionseconds.network.auth

import com.example.billionseconds.domain.auth.AuthErrorType
import com.example.billionseconds.domain.auth.AuthProvider
import com.example.billionseconds.domain.auth.AuthState
import com.example.billionseconds.domain.auth.AuthUser
import com.example.billionseconds.network.api.AuthApi
import com.example.billionseconds.network.model.AppleAuthRequest
import com.example.billionseconds.network.model.GoogleAuthRequest
import com.example.billionseconds.network.model.MergeAccountRequest
import com.example.billionseconds.network.token.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthManager(
    private val tokenManager: TokenManager,
    private val authApi: AuthApi,
) {
    private val _authState = MutableStateFlow<AuthState>(resolveInitialState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private fun resolveInitialState(): AuthState {
        val userId = tokenManager.getUserId() ?: return AuthState.Unauthenticated
        // SyncManager.ensureAuthenticated() stores tokens after anonymous login.
        // We don't persist whether a user is anonymous vs authenticated between launches,
        // so we treat any stored token as Guest until the user explicitly signs in.
        return AuthState.Guest(userId)
    }

    suspend fun signInWithGoogle(idToken: String): Result<AuthState> = runCatching {
        val anonymousToken = tokenManager.getAccessToken()
        val response = if (anonymousToken != null) {
            authApi.mergeAccount(MergeAccountRequest(anonymousToken, idToken))
        } else {
            authApi.loginWithGoogle(GoogleAuthRequest(idToken))
        }
        tokenManager.saveTokens(response)
        val newState = AuthState.Authenticated(
            AuthUser(
                userId = response.userId,
                email = null,
                displayName = null,
                provider = AuthProvider.GOOGLE,
                isAnonymous = false,
            )
        )
        _authState.value = newState
        newState
    }.onFailure { e ->
        _authState.value = AuthState.Error(
            if (isNetworkError(e)) AuthErrorType.NetworkError
            else AuthErrorType.Unknown(e.message ?: "sign_in_failed")
        )
    }

    suspend fun signInWithApple(identityToken: String, name: String?): Result<AuthState> = runCatching {
        val anonymousToken = tokenManager.getAccessToken()
        val response = if (anonymousToken != null) {
            authApi.mergeAccount(MergeAccountRequest(anonymousToken, identityToken))
        } else {
            authApi.loginWithApple(AppleAuthRequest(identityToken, name))
        }
        tokenManager.saveTokens(response)
        val newState = AuthState.Authenticated(
            AuthUser(
                userId = response.userId,
                email = null,
                displayName = name,
                provider = AuthProvider.APPLE,
                isAnonymous = false,
            )
        )
        _authState.value = newState
        newState
    }.onFailure { e ->
        _authState.value = AuthState.Error(
            if (isNetworkError(e)) AuthErrorType.NetworkError
            else AuthErrorType.Unknown(e.message ?: "sign_in_failed")
        )
    }

    suspend fun signOut() {
        try { authApi.logout() } catch (_: Exception) { /* best-effort server revocation */ }
        tokenManager.clearTokens()
        _authState.value = AuthState.Unauthenticated
    }

    fun setGuestState(userId: String) {
        _authState.value = AuthState.Guest(userId)
    }

    fun onSessionExpired() {
        _authState.value = AuthState.Error(AuthErrorType.SessionExpired)
    }

    private fun isNetworkError(e: Throwable): Boolean =
        e is io.ktor.client.plugins.HttpRequestTimeoutException ||
        e.message?.contains("Unable to resolve host") == true ||
        e.message?.contains("SocketException") == true
}
