package com.example.billionseconds.network.token

import android.content.Context
import android.content.SharedPreferences
import com.example.billionseconds.data.AppContext

class AndroidTokenStorage : TokenStorage {

    private val prefs: SharedPreferences =
        AppContext.get().getSharedPreferences("bs_token_prefs", Context.MODE_PRIVATE)

    override fun getAccessToken(): String?  = prefs.getString("access_token", null)
    override fun setAccessToken(token: String?) {
        prefs.edit().apply {
            if (token != null) putString("access_token", token) else remove("access_token")
            apply()
        }
    }

    override fun getRefreshToken(): String? = prefs.getString("refresh_token", null)
    override fun setRefreshToken(token: String?) {
        prefs.edit().apply {
            if (token != null) putString("refresh_token", token) else remove("refresh_token")
            apply()
        }
    }

    override fun getUserId(): String?       = prefs.getString("user_id", null)
    override fun setUserId(userId: String?) {
        prefs.edit().apply {
            if (userId != null) putString("user_id", userId) else remove("user_id")
            apply()
        }
    }

    override fun getDeviceId(): String?     = prefs.getString("device_id", null)
    override fun setDeviceId(id: String) {
        prefs.edit().putString("device_id", id).apply()
    }

    override fun clear() {
        // Preserve deviceId across clear so anonymous login stays stable
        val deviceId = getDeviceId()
        prefs.edit().clear().apply()
        deviceId?.let { setDeviceId(it) }
    }
}

actual fun createTokenStorage(): TokenStorage = AndroidTokenStorage()
