package com.example.billionseconds.data

import com.example.billionseconds.domain.model.TimeCapsule
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

class IosTimeCapsuleStorage : TimeCapsuleStorage {

    private val defaults = NSUserDefaults.standardUserDefaults
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun loadAll(): List<TimeCapsule> {
        val raw = defaults.stringForKey("time_capsules_v1") ?: return emptyList()
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
        defaults.setObject(json.encodeToString(list), "time_capsules_v1")
    }

    override fun delete(id: String) {
        val list = loadAll().filter { it.id != id }
        defaults.setObject(json.encodeToString(list), "time_capsules_v1")
    }

    override fun markOpened(id: String, openedAt: Long) {
        val list = loadAll().map { if (it.id == id) it.copy(openedAt = openedAt) else it }
        defaults.setObject(json.encodeToString(list), "time_capsules_v1")
    }

    override fun clearAll() {
        defaults.removeObjectForKey("time_capsules_v1")
    }
}

actual fun createTimeCapsuleStorage(): TimeCapsuleStorage = IosTimeCapsuleStorage()
