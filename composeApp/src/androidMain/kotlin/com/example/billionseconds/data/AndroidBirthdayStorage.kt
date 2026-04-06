package com.example.billionseconds.data

import android.content.Context
import android.content.SharedPreferences
import com.example.billionseconds.data.model.BirthdayData

class AndroidBirthdayStorage(context: Context) : BirthdayStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("birthday_prefs", Context.MODE_PRIVATE)

    override fun save(data: BirthdayData) {
        prefs.edit()
            .putInt("year", data.year)
            .putInt("month", data.month)
            .putInt("day", data.day)
            .putInt("hour", data.hour)
            .putInt("minute", data.minute)
            .putBoolean("saved", true)
            .apply()
    }

    override fun load(): BirthdayData? {
        if (!prefs.getBoolean("saved", false)) return null
        return BirthdayData(
            year = prefs.getInt("year", 2000),
            month = prefs.getInt("month", 1),
            day = prefs.getInt("day", 1),
            hour = prefs.getInt("hour", 0),
            minute = prefs.getInt("minute", 0)
        )
    }

    override fun clear() = prefs.edit().clear().apply()
}

object AppContext {
    private lateinit var _context: Context
    fun init(context: Context) { _context = context }
    fun get(): Context = _context
}

actual fun createBirthdayStorage(): BirthdayStorage = AndroidBirthdayStorage(AppContext.get())
