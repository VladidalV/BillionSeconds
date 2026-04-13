package com.example.billionseconds.data.event

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EventHistoryRepository(private val storage: EventHistoryStorage) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun getRecord(profileId: String): EventHistoryRecord? {
        val raw = storage.getRecord(profileId) ?: return null
        return try {
            json.decodeFromString<EventHistoryRecord>(raw)
        } catch (e: Exception) {
            null
        }
    }

    fun saveRecord(record: EventHistoryRecord) {
        storage.saveRecord(record.profileId, json.encodeToString(record))
    }

    fun deleteRecord(profileId: String) {
        storage.deleteRecord(profileId)
    }

    fun clearAll() {
        storage.clearAll()
    }
}
