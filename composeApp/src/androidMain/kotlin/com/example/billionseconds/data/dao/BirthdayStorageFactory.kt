package com.example.billionseconds.data.dao

import com.example.billionseconds.MainActivity

actual fun createBirthdayStorage(): BirthdayStorage {
    return AndroidBirthdayStorage(MainActivity.appContext)
}