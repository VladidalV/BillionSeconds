package com.example.billionseconds.data

import com.example.billionseconds.domain.model.TimeCapsule
import com.example.billionseconds.util.currentInstant
import kotlinx.datetime.Clock

class TimeCapsuleRepository(private val storage: TimeCapsuleStorage) {

    fun getAll(): List<TimeCapsule> = storage.loadAll()

    fun save(capsule: TimeCapsule) = storage.save(capsule)

    fun delete(id: String) = storage.delete(id)

    fun markOpened(id: String) {
        val now = currentInstant().toEpochMilliseconds()
        storage.markOpened(id, now)
    }

    fun clearAll() = storage.clearAll()

    fun generateId(): String {
        val now = currentInstant().toEpochMilliseconds()
        val rand = (0..999999).random()
        return "capsule_${now}_$rand"
    }
}
