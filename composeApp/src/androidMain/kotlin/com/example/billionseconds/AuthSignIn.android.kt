package com.example.billionseconds

import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.billionseconds.domain.auth.AuthErrorType
import com.example.billionseconds.mvi.AppIntent
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

actual fun onLaunchGoogleSignIn(dispatch: (AppIntent) -> Unit) {
    val activity = ActivityHolder.get()
    if (activity == null) {
        dispatch(AppIntent.Auth.SignInFailed(AuthErrorType.Unknown("no_activity")))
        return
    }

    CoroutineScope(Dispatchers.Main).launch {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false) // показываем все аккаунты, не только ранее авторизованные
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activity)
            val result = credentialManager.getCredential(context = activity, request = request)
            val credential = result.credential

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            dispatch(AppIntent.Auth.GoogleTokenReceived(idToken))
        } catch (e: GetCredentialCancellationException) {
            dispatch(AppIntent.Auth.SignInFailed(AuthErrorType.Cancelled))
        } catch (e: GetCredentialException) {
            dispatch(AppIntent.Auth.SignInFailed(AuthErrorType.Unknown(e.message ?: "credential_error")))
        } catch (e: Exception) {
            dispatch(AppIntent.Auth.SignInFailed(AuthErrorType.Unknown(e.message ?: "unknown")))
        }
    }
}

actual fun onLaunchAppleSignIn(dispatch: (AppIntent) -> Unit) {
    // Apple Sign-In недоступен на Android.
    dispatch(AppIntent.Auth.SignInFailed(AuthErrorType.Cancelled))
}
