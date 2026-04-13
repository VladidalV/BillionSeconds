package com.example.billionseconds.data

import com.example.billionseconds.data.model.AppSettings

interface AppSettingsStorage {
    fun getSettings(): AppSettings
    fun saveSettings(settings: AppSettings)
    fun clearAll()
}

expect fun createAppSettingsStorage(): AppSettingsStorage
