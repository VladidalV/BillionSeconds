package com.example.billionseconds.data.event

interface EventHistoryStorage {
    fun getRecord(profileId: String): String?
    fun saveRecord(profileId: String, json: String)
    fun getAllProfileIds(): List<String>
    fun deleteRecord(profileId: String)
    fun clearAll()
}

expect fun createEventHistoryStorage(): EventHistoryStorage
