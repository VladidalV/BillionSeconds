package com.example.billionseconds.network.token

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TokenStorage {
    fun getAccessToken(): String?
    fun setAccessToken(token: String?)
    fun getRefreshToken(): String?
    fun setRefreshToken(token: String?)
    fun getUserId(): String?
    fun setUserId(userId: String?)
    fun getDeviceId(): String?
    fun setDeviceId(id: String)
    fun clear()
}

@OptIn(ExperimentalUuidApi::class)
fun TokenStorage.getOrCreateDeviceId(): String {
    return getDeviceId() ?: Uuid.random().toString().also { setDeviceId(it) }
}

expect fun createTokenStorage(): TokenStorage
