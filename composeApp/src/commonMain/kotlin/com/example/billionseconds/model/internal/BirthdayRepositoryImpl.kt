package com.example.billionseconds.model.internal

import com.example.billionseconds.Birthday
import com.example.billionseconds.BirthdayRepository
import com.example.billionseconds.model.internal.db.BirthdayDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class BirthdayRepositoryImpl(
    private val dataStore: BirthdayDataStore
) : BirthdayRepository {

    override fun getBirthday(withCache: Boolean): Flow<Birthday?> {
        if (withCache) {
            return dataStore.birthdayFlow
        }
        return flow {
            emit(dataStore.getBirthday())
        }
    }

    override suspend fun saveBirthday(birthday: Birthday) {
        dataStore.saveBirthday(birthday)
    }

    override suspend fun clearBirthday() {
        dataStore.clearBirthday()
    }
}
