package com.example.billionseconds.network.token

import platform.Foundation.NSUserDefaults

class IosTokenStorage : TokenStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getAccessToken(): String?  = defaults.stringForKey("bs_access_token")
    override fun setAccessToken(token: String?) {
        if (token != null) defaults.setObject(token, "bs_access_token")
        else defaults.removeObjectForKey("bs_access_token")
    }

    override fun getRefreshToken(): String? = defaults.stringForKey("bs_refresh_token")
    override fun setRefreshToken(token: String?) {
        if (token != null) defaults.setObject(token, "bs_refresh_token")
        else defaults.removeObjectForKey("bs_refresh_token")
    }

    override fun getUserId(): String?       = defaults.stringForKey("bs_user_id")
    override fun setUserId(userId: String?) {
        if (userId != null) defaults.setObject(userId, "bs_user_id")
        else defaults.removeObjectForKey("bs_user_id")
    }

    override fun getDeviceId(): String?     = defaults.stringForKey("bs_device_id")
    override fun setDeviceId(id: String) {
        defaults.setObject(id, "bs_device_id")
    }

    override fun clear() {
        // Preserve deviceId across clear
        val deviceId = getDeviceId()
        listOf("bs_access_token", "bs_refresh_token", "bs_user_id")
            .forEach { defaults.removeObjectForKey(it) }
        deviceId?.let { setDeviceId(it) }
    }
}

actual fun createTokenStorage(): TokenStorage = IosTokenStorage()
