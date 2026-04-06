package com.example.billionseconds.data

import com.example.billionseconds.data.model.BirthdayData

class BirthdayRepository(private val storage: BirthdayStorage) {
    fun getBirthday(): BirthdayData? = storage.load()
    fun saveBirthday(data: BirthdayData) = storage.save(data)
    fun clearBirthday() = storage.clear()
}
