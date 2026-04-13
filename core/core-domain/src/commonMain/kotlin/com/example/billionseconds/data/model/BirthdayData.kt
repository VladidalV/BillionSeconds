package com.example.billionseconds.data.model

data class BirthdayData(
    val year: Int,
    val month: Int,   // 1–12
    val day: Int,
    val hour: Int = 0,
    val minute: Int = 0
)
