package com.example.billionseconds.data

import com.example.billionseconds.data.model.AppSettings

class AppSettingsRepository(private val storage: AppSettingsStorage) {
    fun getSettings(): AppSettings = storage.getSettings()
    fun saveSettings(settings: AppSettings) = storage.saveSettings(settings)
    fun clearAll() = storage.clearAll()
}
