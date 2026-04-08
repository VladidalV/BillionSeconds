package com.example.billionseconds.data

import com.example.billionseconds.data.model.BirthdayData
import platform.Foundation.NSUserDefaults

class IosBirthdayStorage : BirthdayStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun save(data: BirthdayData) {
        defaults.setInteger(data.year.toLong(), "year")
        defaults.setInteger(data.month.toLong(), "month")
        defaults.setInteger(data.day.toLong(), "day")
        defaults.setInteger(data.hour.toLong(), "hour")
        defaults.setInteger(data.minute.toLong(), "minute")
        defaults.setBool(true, "saved")
    }

    override fun load(): BirthdayData? {
        if (!defaults.boolForKey("saved")) return null
        return BirthdayData(
            year = defaults.integerForKey("year").toInt(),
            month = defaults.integerForKey("month").toInt(),
            day = defaults.integerForKey("day").toInt(),
            hour = defaults.integerForKey("hour").toInt(),
            minute = defaults.integerForKey("minute").toInt()
        )
    }

    override fun clear() {
        listOf("year", "month", "day", "hour", "minute", "saved", "onboarding_completed")
            .forEach { defaults.removeObjectForKey(it) }
    }

    override fun isOnboardingCompleted(): Boolean =
        defaults.boolForKey("onboarding_completed")

    override fun setOnboardingCompleted(value: Boolean) {
        defaults.setBool(value, "onboarding_completed")
    }
}

actual fun createBirthdayStorage(): BirthdayStorage = IosBirthdayStorage()
