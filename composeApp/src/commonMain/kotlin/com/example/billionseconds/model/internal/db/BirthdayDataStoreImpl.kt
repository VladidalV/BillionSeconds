package com.example.billionseconds.model.internal.db

import com.example.billionseconds.Birthday
import com.example.billionseconds.data.dao.BirthdayStorage
import com.example.billionseconds.data.dao.createBirthdayStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class BirthdayDataStoreImpl(
    private val storage: BirthdayStorage = createBirthdayStorage()
) : BirthdayDataStore {
    
    override val birthdayFlow: Flow<Birthday?> = storage.getBirthdayFlow()
        .map { data ->
            if (data != null) {
                Birthday(birthDate = data.birthDate, birthTime = data.birthTime)
            } else {
                null
            }
        }

    override suspend fun getBirthday(): Birthday? {
        val data = storage.getBirthday()
        return if (data != null) {
            Birthday(birthDate = data.birthDate, birthTime = data.birthTime)
        } else {
            null
        }
    }

    override suspend fun saveBirthday(birthday: Birthday) {
        storage.saveBirthday(birthday.birthDate, birthday.birthTime)
    }

    override suspend fun clearBirthday() {
        storage.clearBirthday()
    }
}
