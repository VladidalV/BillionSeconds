package com.example.billionseconds.ui.auth

sealed class AuthAction {
    data object SignInWithGoogleClicked : AuthAction()
    data object SignInWithAppleClicked : AuthAction()
    data object ContinueAsGuestClicked : AuthAction()
    data object DismissErrorClicked : AuthAction()
}
