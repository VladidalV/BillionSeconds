package com.example.billionseconds.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileRequest(
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
data class ProfileResponse(
    val id: String,
    val name: String,
    @SerialName("relation_type")        val relationType: String,
    @SerialName("custom_relation_name") val customRelationName: String? = null,
    @SerialName("birth_year")           val birthYear: Int,
    @SerialName("birth_month")          val birthMonth: Int,
    @SerialName("birth_day")            val birthDay: Int,
    @SerialName("birth_hour")           val birthHour: Int,
    @SerialName("birth_minute")         val birthMinute: Int,
    @SerialName("unknown_birth_time")   val unknownBirthTime: Boolean,
    @SerialName("is_primary")           val isPrimary: Boolean,
    @SerialName("sort_order")           val sortOrder: Int,
    @SerialName("created_at")           val createdAt: String,
    @SerialName("updated_at")           val updatedAt: String
)

@Serializable
data class ProfilesResponse(
    val profiles: List<ProfileResponse>,
    @SerialName("active_profile_id") val activeProfileId: String?
)

@Serializable
data class SetActiveProfileRequest(
    @SerialName("profile_id") val profileId: String
)
