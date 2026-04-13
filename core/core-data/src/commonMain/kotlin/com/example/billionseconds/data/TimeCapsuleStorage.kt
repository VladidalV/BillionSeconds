package com.example.billionseconds.data

import com.example.billionseconds.domain.model.TimeCapsule

interface TimeCapsuleStorage {
    fun loadAll(): List<TimeCapsule>
    fun save(capsule: TimeCapsule)
    fun delete(id: String)
    fun markOpened(id: String, openedAt: Long)
    fun clearAll()
}

expect fun createTimeCapsuleStorage(): TimeCapsuleStorage
