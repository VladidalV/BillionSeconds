package com.example.billionseconds.data

import com.example.billionseconds.data.model.FamilyProfile

class FamilyProfileRepository(private val storage: FamilyProfileStorage) {

    fun loadProfiles(): List<FamilyProfile>          = storage.loadProfiles()
    fun saveProfiles(profiles: List<FamilyProfile>)  = storage.saveProfiles(profiles)
    fun getActiveProfileId(): String?                 = storage.getActiveProfileId()
    fun setActiveProfileId(id: String)               = storage.setActiveProfileId(id)
    fun clearAll()                                    = storage.clearAll()

    fun getProfileById(id: String): FamilyProfile? =
        storage.loadProfiles().firstOrNull { it.id == id }

    fun addProfile(profile: FamilyProfile) {
        val list = storage.loadProfiles().toMutableList()
        list.add(profile)
        storage.saveProfiles(list)
    }

    fun updateProfile(profile: FamilyProfile) {
        val list = storage.loadProfiles().map { if (it.id == profile.id) profile else it }
        storage.saveProfiles(list)
    }

    fun deleteProfile(id: String) {
        val list = storage.loadProfiles().filter { it.id != id }
        storage.saveProfiles(list)
    }
}
