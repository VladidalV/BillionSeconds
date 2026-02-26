package com.example.billionseconds.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class BirthdayData(
    val id: Int = 1,
    val birthDate: String,
    val birthTime: String
)