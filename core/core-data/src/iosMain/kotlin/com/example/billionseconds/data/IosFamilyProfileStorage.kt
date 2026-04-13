package com.example.billionseconds.data

import com.example.billionseconds.data.model.FamilyProfile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

class IosFamilyProfileStorage : FamilyProfileStorage {

    private val defaults = NSUserDefaults.standardUserDefaults
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun loadProfiles(): List<FamilyProfile> {
        val raw = defaults.stringForKey("family_profiles") ?: return emptyList()
        return try {
            json.decodeFromString<List<FamilyProfile>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun saveProfiles(profiles: List<FamilyProfile>) {
        defaults.setObject(json.encodeToString(profiles), "family_profiles")
    }

    override fun getActiveProfileId(): String? =
        defaults.stringForKey("active_profile_id")

    override fun setActiveProfileId(id: String) {
        defaults.setObject(id, "active_profile_id")
    }

    override fun clearAll() {
        listOf("family_profiles", "active_profile_id").forEach {
            defaults.removeObjectForKey(it)
        }
    }
}

actual fun createFamilyProfileStorage(): FamilyProfileStorage = IosFamilyProfileStorage()
