package com.example.billionseconds.data

import com.example.billionseconds.data.model.BirthdayData

interface BirthdayStorage {
    fun save(data: BirthdayData)
    fun load(): BirthdayData?
    fun clear()
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(value: Boolean)
    fun isUnknownTime(): Boolean
    fun setUnknownTime(value: Boolean)
    fun getLastSeenMilestoneId(): String?
    fun setLastSeenMilestoneId(id: String)
}

expect fun createBirthdayStorage(): BirthdayStorage
