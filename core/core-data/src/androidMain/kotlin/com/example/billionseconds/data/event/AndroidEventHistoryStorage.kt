package com.example.billionseconds.data.event

import android.content.Context
import android.content.SharedPreferences
import com.example.billionseconds.data.AppContext

class AndroidEventHistoryStorage(context: Context) : EventHistoryStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("event_history_prefs", Context.MODE_PRIVATE)

    override fun getRecord(profileId: String): String? =
        prefs.getString(key(profileId), null)

    override fun saveRecord(profileId: String, json: String) {
        prefs.edit().putString(key(profileId), json).apply()
        // Обновляем список известных profileId
        val ids = getAllProfileIds().toMutableSet()
        ids.add(profileId)
        prefs.edit().putString("event_history_ids", ids.joinToString(",")).apply()
    }

    override fun getAllProfileIds(): List<String> {
        val raw = prefs.getString("event_history_ids", null) ?: return emptyList()
        return raw.split(",").filter { it.isNotEmpty() }
    }

    override fun deleteRecord(profileId: String) {
        prefs.edit().remove(key(profileId)).apply()
        val ids = getAllProfileIds().toMutableSet()
        ids.remove(profileId)
        prefs.edit().putString("event_history_ids", ids.joinToString(",")).apply()
    }

    override fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun key(profileId: String) = "event_history_$profileId"
}

actual fun createEventHistoryStorage(): EventHistoryStorage =
    AndroidEventHistoryStorage(AppContext.get())
