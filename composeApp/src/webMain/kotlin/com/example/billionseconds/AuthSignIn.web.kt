package com.example.billionseconds

import com.example.billionseconds.domain.auth.AuthErrorType
import com.example.billionseconds.mvi.AppIntent

// TODO: интегрировать Google Sign-In для Web (Google Identity Services).
actual fun onLaunchGoogleSignIn(dispatch: (AppIntent) -> Unit) {
    dispatch(AppIntent.Auth.SignInFailed(AuthErrorType.Cancelled))
}

actual fun onLaunchAppleSignIn(dispatch: (AppIntent) -> Unit) {
    dispatch(AppIntent.Auth.SignInFailed(AuthErrorType.Cancelled))
}
