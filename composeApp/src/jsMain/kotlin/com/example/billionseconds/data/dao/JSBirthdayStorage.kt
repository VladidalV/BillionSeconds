package com.example.billionseconds.data.dao

import com.example.billionseconds.data.entity.BirthdayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.getOrDefault
import org.w3c.dom.set
import org.w3c.dom.localStorage
import kotlin.js.json

class JSBirthdayStorage : BirthdayStorage {
    companion object {
        private const val KEY_BIRTH_DATE = "birth_date"
        private const val KEY_BIRTH_TIME = "birth_time"
    }

    private val _birthdayFlow = MutableStateFlow<BirthdayData?>(null)

    init {
        loadBirthdayToFlow()
    }

    private fun loadBirthdayToFlow() {
        val birthDate = localStorage[KEY_BIRTH_DATE]
        val birthTime = localStorage[KEY_BIRTH_TIME]
        
        if (birthDate != null && birthTime != null) {
            _birthdayFlow.value = BirthdayData(birthDate = birthDate, birthTime = birthTime)
        }
    }

    override suspend fun saveBirthday(birthDate: String, birthTime: String) {
        localStorage[KEY_BIRTH_DATE] = birthDate
        localStorage[KEY_BIRTH_TIME] = birthTime
        
        _birthdayFlow.value = BirthdayData(birthDate = birthDate, birthTime = birthTime)
    }

    override suspend fun getBirthday(): BirthdayData? {
        val birthDate = localStorage[KEY_BIRTH_DATE]
        val birthTime = localStorage[KEY_BIRTH_TIME]
        
        return if (birthDate != null && birthTime != null) {
            BirthdayData(birthDate = birthDate, birthTime = birthTime)
        } else {
            null
        }
    }

    override suspend fun clearBirthday() {
        localStorage.removeItem(KEY_BIRTH_DATE)
        localStorage.removeItem(KEY_BIRTH_TIME)
        
        _birthdayFlow.value = null
    }

    override fun getBirthdayFlow(): Flow<BirthdayData?> = _birthdayFlow.asStateFlow()
}