package com.example.billionseconds.ui.auth

import com.example.billionseconds.domain.auth.AuthErrorType
import com.example.billionseconds.domain.auth.AuthSource

data class AuthUiState(
    val source: AuthSource = AuthSource.PROFILE,
    val isGoogleLoading: Boolean = false,
    val isAppleLoading: Boolean = false,
    val error: AuthErrorType? = null,
)
