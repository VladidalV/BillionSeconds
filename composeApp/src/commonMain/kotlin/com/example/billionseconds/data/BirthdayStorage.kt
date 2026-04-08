package com.example.billionseconds.data

import com.example.billionseconds.data.model.BirthdayData

interface BirthdayStorage {
    fun save(data: BirthdayData)
    fun load(): BirthdayData?
    fun clear()
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(value: Boolean)
}

expect fun createBirthdayStorage(): BirthdayStorage
