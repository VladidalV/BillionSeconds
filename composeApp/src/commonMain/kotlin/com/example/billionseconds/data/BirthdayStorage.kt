package com.example.billionseconds.data

import com.example.billionseconds.data.model.BirthdayData

interface BirthdayStorage {
    fun save(data: BirthdayData)
    fun load(): BirthdayData?
    fun clear()
}

expect fun createBirthdayStorage(): BirthdayStorage
