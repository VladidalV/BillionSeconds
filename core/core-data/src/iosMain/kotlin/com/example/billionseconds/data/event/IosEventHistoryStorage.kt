package com.example.billionseconds.data.event

import platform.Foundation.NSUserDefaults

class IosEventHistoryStorage : EventHistoryStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getRecord(profileId: String): String? =
        defaults.stringForKey(key(profileId))

    override fun saveRecord(profileId: String, json: String) {
        defaults.setObject(json, key(profileId))
        val ids = getAllProfileIds().toMutableSet()
        ids.add(profileId)
        defaults.setObject(ids.joinToString(","), "event_history_ids")
    }

    override fun getAllProfileIds(): List<String> {
        val raw = defaults.stringForKey("event_history_ids") ?: return emptyList()
        return raw.split(",").filter { it.isNotEmpty() }
    }

    override fun deleteRecord(profileId: String) {
        defaults.removeObjectForKey(key(profileId))
        val ids = getAllProfileIds().toMutableSet()
        ids.remove(profileId)
        defaults.setObject(ids.joinToString(","), "event_history_ids")
    }

    override fun clearAll() {
        getAllProfileIds().forEach { defaults.removeObjectForKey(key(it)) }
        defaults.removeObjectForKey("event_history_ids")
    }

    private fun key(profileId: String) = "event_history_$profileId"
}

actual fun createEventHistoryStorage(): EventHistoryStorage = IosEventHistoryStorage()
