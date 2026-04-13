package com.example.billionseconds.data

import com.example.billionseconds.domain.model.TimeCapsule
import kotlin.time.Clock

class TimeCapsuleRepository(private val storage: TimeCapsuleStorage) {

    fun getAll(): List<TimeCapsule> = storage.loadAll()

    fun save(capsule: TimeCapsule) = storage.save(capsule)

    fun delete(id: String) = storage.delete(id)

    fun markOpened(id: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        storage.markOpened(id, now)
    }

    fun clearAll() = storage.clearAll()

    fun generateId(): String {
        val now = Clock.System.now().toEpochMilliseconds()
        val rand = (0..999999).random()
        return "capsule_${now}_$rand"
    }
}
