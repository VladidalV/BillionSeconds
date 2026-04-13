package com.example.billionseconds.data

import android.content.Context
import android.content.SharedPreferences
import com.example.billionseconds.data.model.FamilyProfile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidFamilyProfileStorage(context: Context) : FamilyProfileStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("family_profile_prefs", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun loadProfiles(): List<FamilyProfile> {
        val raw = prefs.getString("family_profiles", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<FamilyProfile>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun saveProfiles(profiles: List<FamilyProfile>) {
        prefs.edit().putString("family_profiles", json.encodeToString(profiles)).apply()
    }

    override fun getActiveProfileId(): String? =
        prefs.getString("active_profile_id", null)

    override fun setActiveProfileId(id: String) {
        prefs.edit().putString("active_profile_id", id).apply()
    }

    override fun clearAll() {
        prefs.edit().clear().apply()
    }
}

actual fun createFamilyProfileStorage(): FamilyProfileStorage =
    AndroidFamilyProfileStorage(AppContext.get())
