package com.example.billionseconds.model.internal.db

import com.example.billionseconds.Birthday
import kotlinx.coroutines.flow.Flow

internal interface BirthdayDataStore {
    val birthdayFlow: Flow<Birthday?>
    suspend fun getBirthday(): Birthday?
    suspend fun saveBirthday(birthday: Birthday)
    suspend fun clearBirthday()
}
