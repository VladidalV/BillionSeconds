package com.example.billionseconds.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnonymousAuthRequest(
    @SerialName("device_id") val deviceId: String
)

@Serializable
data class AppleAuthRequest(
    @SerialName("identity_token") val identityToken: String,
    val name: String? = null
)

@Serializable
data class GoogleAuthRequest(
    @SerialName("id_token") val idToken: String
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class MergeAccountRequest(
    @SerialName("anonymous_token") val anonymousToken: String,
    @SerialName("provider_token") val providerToken: String,
    val provider: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token")  val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("user_id")       val userId: String,
    val email: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val provider: String? = null
)

@Serializable
data class UpdateDeviceRequest(
    @SerialName("fcm_token")   val fcmToken: String,
    val platform: String,
    @SerialName("app_version") val appVersion: String,
    val timezone: String
)
