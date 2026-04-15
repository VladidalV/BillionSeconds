package com.example.billionseconds.network.token

import kotlinx.browser.localStorage

class WebTokenStorage : TokenStorage {

    override fun getAccessToken(): String?  = localStorage.getItem("bs_access_token")
    override fun setAccessToken(token: String?) {
        if (token != null) localStorage.setItem("bs_access_token", token)
        else localStorage.removeItem("bs_access_token")
    }

    override fun getRefreshToken(): String? = localStorage.getItem("bs_refresh_token")
    override fun setRefreshToken(token: String?) {
        if (token != null) localStorage.setItem("bs_refresh_token", token)
        else localStorage.removeItem("bs_refresh_token")
    }

    override fun getUserId(): String?       = localStorage.getItem("bs_user_id")
    override fun setUserId(userId: String?) {
        if (userId != null) localStorage.setItem("bs_user_id", userId)
        else localStorage.removeItem("bs_user_id")
    }

    override fun getDeviceId(): String?     = localStorage.getItem("bs_device_id")
    override fun setDeviceId(id: String) {
        localStorage.setItem("bs_device_id", id)
    }

    override fun clear() {
        // Preserve deviceId across clear
        val deviceId = getDeviceId()
        listOf("bs_access_token", "bs_refresh_token", "bs_user_id")
            .forEach { localStorage.removeItem(it) }
        deviceId?.let { setDeviceId(it) }
    }
}

actual fun createTokenStorage(): TokenStorage = WebTokenStorage()
