package com.example.billionseconds.mvi

import com.example.billionseconds.ui.auth.AuthAction

fun authAdapter(dispatch: (AppIntent) -> Unit): (AuthAction) -> Unit = { action ->
    when (action) {
        AuthAction.SignInWithGoogleClicked -> dispatch(AppIntent.Auth.SignInWithGoogleClicked)
        AuthAction.SignInWithAppleClicked  -> dispatch(AppIntent.Auth.SignInWithAppleClicked)
        AuthAction.ContinueAsGuestClicked  -> dispatch(AppIntent.Auth.ContinueAsGuestClicked)
        AuthAction.DismissErrorClicked     -> dispatch(AppIntent.Auth.DismissError)
    }
}
