package com.example.billionseconds.data

import android.content.Context
import android.content.SharedPreferences
import com.example.billionseconds.domain.model.TimeCapsule
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidTimeCapsuleStorage(context: Context) : TimeCapsuleStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("time_capsule_prefs", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun loadAll(): List<TimeCapsule> {
        val raw = prefs.getString("time_capsules_v1", null) ?: return emptyList()
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
        prefs.edit().putString("time_capsules_v1", json.encodeToString(list)).apply()
    }

    override fun delete(id: String) {
        val list = loadAll().filter { it.id != id }
        prefs.edit().putString("time_capsules_v1", json.encodeToString(list)).apply()
    }

    override fun markOpened(id: String, openedAt: Long) {
        val list = loadAll().map { if (it.id == id) it.copy(openedAt = openedAt) else it }
        prefs.edit().putString("time_capsules_v1", json.encodeToString(list)).apply()
    }

    override fun clearAll() {
        prefs.edit().remove("time_capsules_v1").apply()
    }
}

actual fun createTimeCapsuleStorage(): TimeCapsuleStorage =
    AndroidTimeCapsuleStorage(AppContext.get())
