package com.example.billionseconds.data

import com.example.billionseconds.data.model.AppSettings
import platform.Foundation.NSUserDefaults

class IosAppSettingsStorage : AppSettingsStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getSettings(): AppSettings = AppSettings(
        notificationsEnabled      = defaults.boolForKey("notifications_enabled"),
        milestoneRemindersEnabled = defaults.boolForKey("milestone_reminders_enabled").let {
            if (!defaults.objectForKey("milestone_reminders_enabled").let { o -> o != null }) true else it
        },
        familyRemindersEnabled    = defaults.boolForKey("family_reminders_enabled").let {
            if (!defaults.objectForKey("family_reminders_enabled").let { o -> o != null }) true else it
        },
        reengagementEnabled       = defaults.boolForKey("reengagement_enabled").let {
            if (!defaults.objectForKey("reengagement_enabled").let { o -> o != null }) true else it
        },
        approximateLabelsEnabled  = defaults.boolForKey("approximate_labels_enabled").let {
            if (!defaults.objectForKey("approximate_labels_enabled").let { o -> o != null }) true else it
        },
        use24HourFormat           = defaults.boolForKey("use_24_hour_format")
    )

    override fun saveSettings(settings: AppSettings) {
        defaults.setBool(settings.notificationsEnabled,      "notifications_enabled")
        defaults.setBool(settings.milestoneRemindersEnabled, "milestone_reminders_enabled")
        defaults.setBool(settings.familyRemindersEnabled,    "family_reminders_enabled")
        defaults.setBool(settings.reengagementEnabled,       "reengagement_enabled")
        defaults.setBool(settings.approximateLabelsEnabled,  "approximate_labels_enabled")
        defaults.setBool(settings.use24HourFormat,           "use_24_hour_format")
    }

    override fun clearAll() {
        listOf(
            "notifications_enabled", "milestone_reminders_enabled",
            "family_reminders_enabled", "reengagement_enabled",
            "approximate_labels_enabled", "use_24_hour_format"
        ).forEach { defaults.removeObjectForKey(it) }
    }
}

actual fun createAppSettingsStorage(): AppSettingsStorage = IosAppSettingsStorage()
