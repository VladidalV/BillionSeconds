package com.example.billionseconds.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class RelationType(val displayLabel: String, val emoji: String) {
    SELF(    "Я",            "👤"),
    CHILD(   "Ребёнок",     "👶"),
    PARTNER( "Партнёр",     "❤️"),
    MOTHER(  "Мама",        "👩"),
    FATHER(  "Папа",        "👨"),
    SIBLING( "Брат/Сестра", "🧑"),
    OTHER(   "Другое",      "👥")
}

@Serializable
data class FamilyProfile(
    val id: String,
    val name: String,
    val relationType: RelationType,
    val customRelationName: String?  = null,
    val birthYear: Int,
    val birthMonth: Int,
    val birthDay: Int,
    val birthHour: Int               = 12,
    val birthMinute: Int             = 0,
    val unknownBirthTime: Boolean    = false,
    val isPrimary: Boolean           = false,
    val sortOrder: Int               = 0,
    val createdAtEpochSeconds: Long
)

fun FamilyProfile.toBirthdayData() = BirthdayData(
    year   = birthYear,
    month  = birthMonth,
    day    = birthDay,
    hour   = if (unknownBirthTime) 12 else birthHour,
    minute = if (unknownBirthTime) 0  else birthMinute
)
