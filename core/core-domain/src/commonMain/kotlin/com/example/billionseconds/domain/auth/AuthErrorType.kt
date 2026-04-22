package com.example.billionseconds.domain.auth

sealed class AuthErrorType {
    data object NetworkError : AuthErrorType()
    data object Cancelled : AuthErrorType()
    data object AccountAlreadyExists : AuthErrorType()
    data object SessionExpired : AuthErrorType()
    data class Unknown(val message: String) : AuthErrorType()
}
