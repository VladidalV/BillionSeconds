package com.example.billionseconds.data.dao

import com.example.billionseconds.data.entity.BirthdayData
import kotlinx.coroutines.flow.Flow

interface BirthdayStorage {
    suspend fun saveBirthday(birthDate: String, birthTime: String)
    suspend fun getBirthday(): BirthdayData?
    suspend fun clearBirthday()
    fun getBirthdayFlow(): Flow<BirthdayData?>
}