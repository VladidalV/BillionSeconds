package com.example.billionseconds.domain.auth

sealed class AuthState {
    /** Токен есть, но пользователь анонимный (anonymous session). */
    data class Guest(val userId: String) : AuthState()

    /** Пользователь вошёл через Google или Apple. */
    data class Authenticated(val user: AuthUser) : AuthState()

    /** Нет токена вообще (первый запуск до anonymous login). */
    data object Unauthenticated : AuthState()

    /** Идёт попытка входа. */
    data object Loading : AuthState()

    /** Ошибка авторизации. */
    data class Error(val type: AuthErrorType) : AuthState()
}

val AuthState.isSignedIn: Boolean
    get() = this is AuthState.Authenticated

val AuthState.isGuest: Boolean
    get() = this is AuthState.Guest || this is AuthState.Unauthenticated
