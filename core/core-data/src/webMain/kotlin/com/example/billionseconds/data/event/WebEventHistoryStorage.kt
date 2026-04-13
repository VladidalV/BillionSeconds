package com.example.billionseconds.data.event

import kotlinx.browser.localStorage

class WebEventHistoryStorage : EventHistoryStorage {

    override fun getRecord(profileId: String): String? =
        localStorage.getItem(key(profileId))

    override fun saveRecord(profileId: String, json: String) {
        localStorage.setItem(key(profileId), json)
        val ids = getAllProfileIds().toMutableSet()
        ids.add(profileId)
        localStorage.setItem("event_history_ids", ids.joinToString(","))
    }

    override fun getAllProfileIds(): List<String> {
        val raw = localStorage.getItem("event_history_ids") ?: return emptyList()
        return raw.split(",").filter { it.isNotEmpty() }
    }

    override fun deleteRecord(profileId: String) {
        localStorage.removeItem(key(profileId))
        val ids = getAllProfileIds().toMutableSet()
        ids.remove(profileId)
        localStorage.setItem("event_history_ids", ids.joinToString(","))
    }

    override fun clearAll() {
        getAllProfileIds().forEach { localStorage.removeItem(key(it)) }
        localStorage.removeItem("event_history_ids")
    }

    private fun key(profileId: String) = "event_history_$profileId"
}

actual fun createEventHistoryStorage(): EventHistoryStorage = WebEventHistoryStorage()
