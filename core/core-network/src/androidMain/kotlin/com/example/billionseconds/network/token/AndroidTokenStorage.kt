package com.example.billionseconds.network.token

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.billionseconds.data.AppContext

class AndroidTokenStorage : TokenStorage {

    // lazy — EncryptedSharedPreferences инициализируется при первом обращении,
    // после того как Android Keystore готов к работе.
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(AppContext.get())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            AppContext.get(),
            "bs_secure_token_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun getAccessToken(): String?  = prefs.getString("access_token", null)
    override fun setAccessToken(token: String?) = edit("access_token", token)

    override fun getRefreshToken(): String? = prefs.getString("refresh_token", null)
    override fun setRefreshToken(token: String?) = edit("refresh_token", token)

    override fun getUserId(): String?       = prefs.getString("user_id", null)
    override fun setUserId(userId: String?) = edit("user_id", userId)

    override fun getDeviceId(): String?     = prefs.getString("device_id", null)
    override fun setDeviceId(id: String) {
        prefs.edit().putString("device_id", id).apply()
    }

    override fun clear() {
        val deviceId = getDeviceId()
        prefs.edit().clear().apply()
        deviceId?.let { setDeviceId(it) }
    }

    private fun edit(key: String, value: String?) {
        prefs.edit().apply {
            if (value != null) putString(key, value) else remove(key)
            apply()
        }
    }
}

actual fun createTokenStorage(): TokenStorage = AndroidTokenStorage()
