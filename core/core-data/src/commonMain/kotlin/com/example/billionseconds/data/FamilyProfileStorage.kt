package com.example.billionseconds.data

import com.example.billionseconds.data.model.FamilyProfile

interface FamilyProfileStorage {
    fun loadProfiles(): List<FamilyProfile>
    fun saveProfiles(profiles: List<FamilyProfile>)
    fun getActiveProfileId(): String?
    fun setActiveProfileId(id: String)
    fun clearAll()
}

expect fun createFamilyProfileStorage(): FamilyProfileStorage
