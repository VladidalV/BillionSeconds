package com.example.billionseconds.domain.auth

data class AuthUser(
    val userId: String,
    val email: String?,
    val displayName: String?,
    val provider: AuthProvider,
    val isAnonymous: Boolean,
)
