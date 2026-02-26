package com.example.billionseconds.data.dao

import android.content.Context
import android.content.SharedPreferences
import com.example.billionseconds.data.entity.BirthdayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidBirthdayStorage(private val context: Context) : BirthdayStorage {
    companion object {
        private const val PREFS_NAME = "birthday_prefs"
        private const val KEY_BIRTH_DATE = "birth_date"
        private const val KEY_BIRTH_TIME = "birth_time"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _birthdayFlow = MutableStateFlow<BirthdayData?>(null)

    init {
        loadBirthdayToFlow()
    }

    private fun loadBirthdayToFlow() {
        val birthDate = prefs.getString(KEY_BIRTH_DATE, null)
        val birthTime = prefs.getString(KEY_BIRTH_TIME, null)
        
        if (birthDate != null && birthTime != null) {
            _birthdayFlow.value = BirthdayData(birthDate = birthDate, birthTime = birthTime)
        }
    }

    override suspend fun saveBirthday(birthDate: String, birthTime: String) {
        prefs.edit()
            .putString(KEY_BIRTH_DATE, birthDate)
            .putString(KEY_BIRTH_TIME, birthTime)
            .apply()
        
        _birthdayFlow.value = BirthdayData(birthDate = birthDate, birthTime = birthTime)
    }

    override suspend fun getBirthday(): BirthdayData? {
        val birthDate = prefs.getString(KEY_BIRTH_DATE, null)
        val birthTime = prefs.getString(KEY_BIRTH_TIME, null)
        
        return if (birthDate != null && birthTime != null) {
            BirthdayData(birthDate = birthDate, birthTime = birthTime)
        } else {
            null
        }
    }

    override suspend fun clearBirthday() {
        prefs.edit()
            .remove(KEY_BIRTH_DATE)
            .remove(KEY_BIRTH_TIME)
            .apply()
        
        _birthdayFlow.value = null
    }

    override fun getBirthdayFlow(): Flow<BirthdayData?> = _birthdayFlow.asStateFlow()
}