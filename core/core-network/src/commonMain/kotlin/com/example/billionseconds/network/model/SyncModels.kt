package com.example.billionseconds.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncResponse(
    @SerialName("server_time")        val serverTime: String,
    val user: UserResponse?,
    val settings: SettingsResponse?,
    val profiles: List<ProfileResponse>,
    @SerialName("event_history")      val eventHistory: List<EventHistoryResponse>,
    val capsules: List<CapsuleResponse>,
    @SerialName("milestone_progress") val milestoneProgress: List<MilestoneProgressResponse>
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String?,
    @SerialName("display_name")  val displayName: String?,
    @SerialName("auth_provider") val authProvider: String,
    val platform: String?,
    @SerialName("app_version")   val appVersion: String?,
    val timezone: String?,
    val locale: String?,
    @SerialName("created_at")    val createdAt: String,
    @SerialName("updated_at")    val updatedAt: String
)

// --- Migration ---

@Serializable
data class MigrateRequest(
    val profiles: List<MigrateProfileItem>,
    @SerialName("active_profile_local_id") val activeProfileLocalId: String? = null,
    val settings: MigrateSettingsItem? = null,
    @SerialName("event_history")           val eventHistory: List<MigrateEventHistoryItem> = emptyList(),
    val capsules: List<MigrateCapsuleItem> = emptyList(),
    @SerialName("milestone_progress")      val milestoneProgress: List<MigrateMilestoneProgressItem> = emptyList()
)

@Serializable
data class MigrateProfileItem(
    @SerialName("local_id")             val localId: String,
    val name: String,
    @SerialName("relation_type")        val relationType: String,
    @SerialName("custom_relation_name") val customRelationName: String? = null,
    @SerialName("birth_year")           val birthYear: Int,
    @SerialName("birth_month")          val birthMonth: Int,
    @SerialName("birth_day")            val birthDay: Int,
    @SerialName("birth_hour")           val birthHour: Int = 12,
    @SerialName("birth_minute")         val birthMinute: Int = 0,
    @SerialName("unknown_birth_time")   val unknownBirthTime: Boolean = false,
    @SerialName("sort_order")           val sortOrder: Int = 0
)

@Serializable
data class MigrateSettingsItem(
    @SerialName("onboarding_completed")        val onboardingCompleted: Boolean? = null,
    @SerialName("notifications_enabled")       val notificationsEnabled: Boolean? = null,
    @SerialName("milestone_reminders_enabled") val milestoneRemindersEnabled: Boolean? = null,
    @SerialName("family_reminders_enabled")    val familyRemindersEnabled: Boolean? = null,
    @SerialName("reengagement_enabled")        val reengagementEnabled: Boolean? = null,
    @SerialName("approximate_labels_enabled")  val approximateLabelsEnabled: Boolean? = null,
    @SerialName("use_24_hour_format")          val use24HourFormat: Boolean? = null
)

@Serializable
data class MigrateEventHistoryItem(
    @SerialName("profile_local_id")      val profileLocalId: String,
    @SerialName("first_shown_at")        val firstShownAt: String? = null,
    @SerialName("celebration_shown_at")  val celebrationShownAt: String? = null,
    @SerialName("share_prompt_shown_at") val sharePromptShownAt: String? = null
)

@Serializable
data class MigrateCapsuleItem(
    @SerialName("local_id")                    val localId: String,
    val title: String,
    val message: String,
    @SerialName("recipient_profile_local_id")  val recipientProfileLocalId: String? = null,
    @SerialName("is_draft")                    val isDraft: Boolean = false,
    @SerialName("unlock_condition_type")       val unlockConditionType: String,
    @SerialName("unlock_at_epoch_ms")          val unlockAtEpochMs: Long? = null,
    @SerialName("unlock_profile_local_id")     val unlockProfileLocalId: String? = null
)

@Serializable
data class MigrateMilestoneProgressItem(
    @SerialName("profile_local_id")     val profileLocalId: String,
    @SerialName("last_seen_reached_id") val lastSeenReachedId: String? = null
)

@Serializable
data class IdMappingEntry(
    @SerialName("local_id")  val localId: String,
    @SerialName("server_id") val serverId: String
)

@Serializable
data class MigrateResponse(
    @SerialName("id_mapping")         val idMapping: List<IdMappingEntry>,
    @SerialName("server_time")        val serverTime: String,
    val user: UserResponse?,
    val settings: SettingsResponse?,
    val profiles: List<ProfileResponse>,
    @SerialName("event_history")      val eventHistory: List<EventHistoryResponse>,
    val capsules: List<CapsuleResponse>,
    @SerialName("milestone_progress") val milestoneProgress: List<MilestoneProgressResponse>
)
