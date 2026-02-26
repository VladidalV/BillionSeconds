package com.example.billionseconds.data.dao

actual fun createBirthdayStorage(): BirthdayStorage {
    return JSBirthdayStorage()
}