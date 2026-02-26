package com.example.billionseconds.data.repository

import com.example.billionseconds.data.dao.BirthdayStorage
import com.example.billionseconds.data.dao.createBirthdayStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class BirthdayRepository(
    private val storage: BirthdayStorage = createBirthdayStorage(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    
    val birthdayFlow: Flow<BirthDateAndTime?> = storage.getBirthdayFlow()
        .distinctUntilChanged()
        .map { data ->
            if (data != null) {
                BirthDateAndTime(data.birthDate, data.birthTime)
            } else {
                null
            }
        }
    
    suspend fun saveBirthday(birthDate: String, birthTime: String) {
        storage.saveBirthday(birthDate, birthTime)
    }
    
    suspend fun getBirthday(): BirthDateAndTime? {
        val data = storage.getBirthday()
        return if (data != null) {
            BirthDateAndTime(data.birthDate, data.birthTime)
        } else {
            null
        }
    }
    
    suspend fun clearBirthday() {
        storage.clearBirthday()
    }
}

data class BirthDateAndTime(
    val birthDate: String,
    val birthTime: String
)