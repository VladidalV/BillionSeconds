package com.example.billionseconds.data

import com.example.billionseconds.data.model.FamilyProfile
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WebFamilyProfileStorage : FamilyProfileStorage {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun loadProfiles(): List<FamilyProfile> {
        val raw = localStorage.getItem("family_profiles") ?: return emptyList()
        return try {
            json.decodeFromString<List<FamilyProfile>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun saveProfiles(profiles: List<FamilyProfile>) {
        localStorage.setItem("family_profiles", json.encodeToString(profiles))
    }

    override fun getActiveProfileId(): String? =
        localStorage.getItem("active_profile_id")

    override fun setActiveProfileId(id: String) {
        localStorage.setItem("active_profile_id", id)
    }

    override fun clearAll() {
        localStorage.removeItem("family_profiles")
        localStorage.removeItem("active_profile_id")
    }
}

actual fun createFamilyProfileStorage(): FamilyProfileStorage = WebFamilyProfileStorage()
