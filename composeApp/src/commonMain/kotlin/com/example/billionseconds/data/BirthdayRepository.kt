package com.example.billionseconds.data

import com.example.billionseconds.data.model.BirthdayData

class BirthdayRepository(private val storage: BirthdayStorage) {
    fun getBirthday(): BirthdayData? = storage.load()
    fun saveBirthday(data: BirthdayData) = storage.save(data)
    fun clearBirthday() = storage.clear()
    fun isOnboardingCompleted(): Boolean = storage.isOnboardingCompleted()
    fun setOnboardingCompleted(value: Boolean) = storage.setOnboardingCompleted(value)
    fun isUnknownTime(): Boolean = storage.isUnknownTime()
    fun setUnknownTime(value: Boolean) = storage.setUnknownTime(value)
}
