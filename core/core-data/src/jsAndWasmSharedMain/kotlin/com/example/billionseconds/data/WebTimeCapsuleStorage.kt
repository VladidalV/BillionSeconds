package com.example.billionseconds.data

import com.example.billionseconds.domain.model.TimeCapsule
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WebTimeCapsuleStorage : TimeCapsuleStorage {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun loadAll(): List<TimeCapsule> {
        val raw = localStorage.getItem("time_capsules_v1") ?: return emptyList()
        return try {
            json.decodeFromString<List<TimeCapsule>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun save(capsule: TimeCapsule) {
        val list = loadAll().toMutableList()
        val idx = list.indexOfFirst { it.id == capsule.id }
        if (idx >= 0) list[idx] = capsule else list.add(capsule)
        localStorage.setItem("time_capsules_v1", json.encodeToString(list))
    }

    override fun delete(id: String) {
        val list = loadAll().filter { it.id != id }
        localStorage.setItem("time_capsules_v1", json.encodeToString(list))
    }

    override fun markOpened(id: String, openedAt: Long) {
        val list = loadAll().map { if (it.id == id) it.copy(openedAt = openedAt) else it }
        localStorage.setItem("time_capsules_v1", json.encodeToString(list))
    }

    override fun clearAll() {
        localStorage.removeItem("time_capsules_v1")
    }
}

actual fun createTimeCapsuleStorage(): TimeCapsuleStorage = WebTimeCapsuleStorage()
