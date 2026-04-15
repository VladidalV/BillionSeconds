package com.example.billionseconds.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SettingsResponse(
    @SerialName("active_profile_id")           val activeProfileId: String?,
    @SerialName("onboarding_completed")        val onboardingCompleted: Boolean,
    @SerialName("notifications_enabled")       val notificationsEnabled: Boolean,
    @SerialName("milestone_reminders_enabled") val milestoneRemindersEnabled: Boolean,
    @SerialName("family_reminders_enabled")    val familyRemindersEnabled: Boolean,
    @SerialName("reengagement_enabled")        val reengagementEnabled: Boolean,
    @SerialName("approximate_labels_enabled")  val approximateLabelsEnabled: Boolean,
    @SerialName("use_24_hour_format")          val use24HourFormat: Boolean,
    @SerialName("updated_at")                  val updatedAt: String
)

@Serializable
data class SettingsPatchRequest(
    @SerialName("active_profile_id")           val activeProfileId: String? = null,
    @SerialName("onboarding_completed")        val onboardingCompleted: Boolean? = null,
    @SerialName("notifications_enabled")       val notificationsEnabled: Boolean? = null,
    @SerialName("milestone_reminders_enabled") val milestoneRemindersEnabled: Boolean? = null,
    @SerialName("family_reminders_enabled")    val familyRemindersEnabled: Boolean? = null,
    @SerialName("reengagement_enabled")        val reengagementEnabled: Boolean? = null,
    @SerialName("approximate_labels_enabled")  val approximateLabelsEnabled: Boolean? = null,
    @SerialName("use_24_hour_format")          val use24HourFormat: Boolean? = null
)
