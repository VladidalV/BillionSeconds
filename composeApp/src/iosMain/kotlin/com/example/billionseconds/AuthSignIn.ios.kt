package com.example.billionseconds

import com.example.billionseconds.domain.auth.AuthErrorType
import com.example.billionseconds.mvi.AppIntent
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject

// Удерживает делегат живым во время async flow — без этого ARC уберёт его до коллбека.
private var appleSignInDelegate: AppleSignInDelegate? = null

@OptIn(BetaInteropApi::class)
private class AppleSignInDelegate(
    private val dispatch: (AppIntent) -> Unit
) : NSObject(),
    ASAuthorizationControllerDelegateProtocol,
    ASAuthorizationControllerPresentationContextProvidingProtocol {

    fun present() {
        val provider = ASAuthorizationAppleIDProvider()
        val request = provider.createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
        }
        val controller = ASAuthorizationController(listOf(request))
        controller.delegate = this
        controller.presentationContextProvider = this
        controller.performRequests()
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: platform.AuthenticationServices.ASAuthorization
    ) {
        val credential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
        if (credential == null) {
            dispatch(AppIntent.Auth.SignInFailed(AuthErrorType.Unknown("unexpected_credential")))
            appleSignInDelegate = null
            return
        }

        val tokenData = credential.identityToken
        if (tokenData == null) {
            dispatch(AppIntent.Auth.SignInFailed(AuthErrorType.Unknown("no_identity_token")))
            appleSignInDelegate = null
            return
        }

        // NSString.create() returns NSString? which doesn't implicitly bridge to String?.
        // .description is NSObject's bridged String property — returns the string value itself.
        val identityToken: String? = NSString.create(tokenData, NSUTF8StringEncoding)?.description
        if (identityToken == null) {
            dispatch(AppIntent.Auth.SignInFailed(AuthErrorType.Unknown("token_decode_failed")))
            appleSignInDelegate = null
            return
        }

        val fullName = credential.fullName
        val name = fullName?.let {
            listOfNotNull(it.givenName, it.familyName)
                .joinToString(" ")
                .takeIf { n -> n.isNotBlank() }
        }

        dispatch(AppIntent.Auth.AppleTokenReceived(identityToken, name))
        appleSignInDelegate = null
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError
    ) {
        // ASAuthorizationErrorCanceled = 1001
        if (didCompleteWithError.code == 1001L) {
            dispatch(AppIntent.Auth.SignInFailed(AuthErrorType.Cancelled))
        } else {
            dispatch(AppIntent.Auth.SignInFailed(
                AuthErrorType.Unknown("apple_error_${didCompleteWithError.code}")
            ))
        }
        appleSignInDelegate = null
    }

    override fun presentationAnchorForAuthorizationController(
        controller: ASAuthorizationController
    ): UIWindow {
        return UIApplication.sharedApplication.keyWindow
            ?: UIApplication.sharedApplication.windows.filterIsInstance<UIWindow>().firstOrNull()
            ?: error("No window available for Apple Sign-In presentation")
    }
}

actual fun onLaunchGoogleSignIn(dispatch: (AppIntent) -> Unit) {
    // TODO (EXTERNAL_SETUP.md): Google Sign-In iOS — нужен GoogleService-Info.plist и GIDSignIn SDK
    dispatch(AppIntent.Auth.SignInFailed(AuthErrorType.Cancelled))
}

actual fun onLaunchAppleSignIn(dispatch: (AppIntent) -> Unit) {
    appleSignInDelegate = AppleSignInDelegate(dispatch)
    appleSignInDelegate!!.present()
}
