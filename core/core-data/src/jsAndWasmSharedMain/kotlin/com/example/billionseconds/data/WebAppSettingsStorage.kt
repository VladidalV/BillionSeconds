package com.example.billionseconds.data

import com.example.billionseconds.data.model.AppSettings
import kotlinx.browser.localStorage

class WebAppSettingsStorage : AppSettingsStorage {

    private fun getBool(key: String, default: Boolean): Boolean =
        localStorage.getItem(key)?.toBooleanStrictOrNull() ?: default

    override fun getSettings(): AppSettings = AppSettings(
        notificationsEnabled      = getBool("notifications_enabled", false),
        milestoneRemindersEnabled = getBool("milestone_reminders_enabled", true),
        familyRemindersEnabled    = getBool("family_reminders_enabled", true),
        reengagementEnabled       = getBool("reengagement_enabled", true),
        approximateLabelsEnabled  = getBool("approximate_labels_enabled", true),
        use24HourFormat           = getBool("use_24_hour_format", false)
    )

    override fun saveSettings(settings: AppSettings) {
        localStorage.setItem("notifications_enabled",      settings.notificationsEnabled.toString())
        localStorage.setItem("milestone_reminders_enabled", settings.milestoneRemindersEnabled.toString())
        localStorage.setItem("family_reminders_enabled",   settings.familyRemindersEnabled.toString())
        localStorage.setItem("reengagement_enabled",       settings.reengagementEnabled.toString())
        localStorage.setItem("approximate_labels_enabled", settings.approximateLabelsEnabled.toString())
        localStorage.setItem("use_24_hour_format",         settings.use24HourFormat.toString())
    }

    override fun clearAll() {
        listOf(
            "notifications_enabled", "milestone_reminders_enabled",
            "family_reminders_enabled", "reengagement_enabled",
            "approximate_labels_enabled", "use_24_hour_format"
        ).forEach { localStorage.removeItem(it) }
    }
}

actual fun createAppSettingsStorage(): AppSettingsStorage = WebAppSettingsStorage()
