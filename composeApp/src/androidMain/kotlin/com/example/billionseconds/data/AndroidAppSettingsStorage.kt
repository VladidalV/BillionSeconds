package com.example.billionseconds.data

import android.content.Context
import android.content.SharedPreferences
import com.example.billionseconds.data.model.AppSettings

class AndroidAppSettingsStorage(context: Context) : AppSettingsStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    override fun getSettings(): AppSettings = AppSettings(
        notificationsEnabled      = prefs.getBoolean("notifications_enabled", false),
        milestoneRemindersEnabled = prefs.getBoolean("milestone_reminders_enabled", true),
        familyRemindersEnabled    = prefs.getBoolean("family_reminders_enabled", true),
        reengagementEnabled       = prefs.getBoolean("reengagement_enabled", true),
        approximateLabelsEnabled  = prefs.getBoolean("approximate_labels_enabled", true),
        use24HourFormat           = prefs.getBoolean("use_24_hour_format", false)
    )

    override fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putBoolean("notifications_enabled",      settings.notificationsEnabled)
            .putBoolean("milestone_reminders_enabled", settings.milestoneRemindersEnabled)
            .putBoolean("family_reminders_enabled",   settings.familyRemindersEnabled)
            .putBoolean("reengagement_enabled",       settings.reengagementEnabled)
            .putBoolean("approximate_labels_enabled", settings.approximateLabelsEnabled)
            .putBoolean("use_24_hour_format",         settings.use24HourFormat)
            .apply()
    }

    override fun clearAll() {
        prefs.edit().clear().apply()
    }
}

actual fun createAppSettingsStorage(): AppSettingsStorage =
    AndroidAppSettingsStorage(AppContext.get())
