package com.example.billionseconds.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val notificationsEnabled: Boolean      = false,
    val milestoneRemindersEnabled: Boolean = true,
    val familyRemindersEnabled: Boolean    = true,
    val reengagementEnabled: Boolean       = true,
    val approximateLabelsEnabled: Boolean  = true,
    val use24HourFormat: Boolean           = false
)
