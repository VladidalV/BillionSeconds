package com.example.billionseconds

import kotlinx.coroutines.flow.Flow

public interface BirthdayRepository {
    public fun getBirthday(withCache: Boolean = true): Flow<Birthday?>
    public suspend fun saveBirthday(birthday: Birthday)
    public suspend fun clearBirthday()
}
