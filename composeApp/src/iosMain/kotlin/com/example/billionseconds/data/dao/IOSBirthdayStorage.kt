package com.example.billionseconds.data.dao

import com.example.billionseconds.data.entity.BirthdayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

class IOSBirthdayStorage : BirthdayStorage {
    companion object {
        private const val KEY_BIRTH_DATE = "birth_date"
        private const val KEY_BIRTH_TIME = "birth_time"
    }

    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
    private val _birthdayFlow = MutableStateFlow<BirthdayData?>(null)

    init {
        loadBirthdayToFlow()
    }

    private fun loadBirthdayToFlow() {
        val birthDate = defaults.stringForKey(KEY_BIRTH_DATE)
        val birthTime = defaults.stringForKey(KEY_BIRTH_TIME)
        
        if (birthDate != null && birthTime != null) {
            _birthdayFlow.value = BirthdayData(birthDate = birthDate, birthTime = birthTime)
        }
    }

    override suspend fun saveBirthday(birthDate: String, birthTime: String) {
        defaults.setObject(birthDate, KEY_BIRTH_DATE)
        defaults.setObject(birthTime, KEY_BIRTH_TIME)
        defaults.synchronize()
        
        _birthdayFlow.value = BirthdayData(birthDate = birthDate, birthTime = birthTime)
    }

    override suspend fun getBirthday(): BirthdayData? {
        val birthDate = defaults.stringForKey(KEY_BIRTH_DATE)
        val birthTime = defaults.stringForKey(KEY_BIRTH_TIME)
        
        return if (birthDate != null && birthTime != null) {
            BirthdayData(birthDate = birthDate, birthTime = birthTime)
        } else {
            null
        }
    }

    override suspend fun clearBirthday() {
        defaults.removeObjectForKey(KEY_BIRTH_DATE)
        defaults.removeObjectForKey(KEY_BIRTH_TIME)
        defaults.synchronize()
        
        _birthdayFlow.value = null
    }

    override fun getBirthdayFlow(): Flow<BirthdayData?> = _birthdayFlow.asStateFlow()
}