package com.example.billionseconds.data

import com.example.billionseconds.data.model.BirthdayData
import kotlinx.browser.localStorage

class WebBirthdayStorage : BirthdayStorage {

    override fun save(data: BirthdayData) {
        localStorage.setItem("year", data.year.toString())
        localStorage.setItem("month", data.month.toString())
        localStorage.setItem("day", data.day.toString())
        localStorage.setItem("hour", data.hour.toString())
        localStorage.setItem("minute", data.minute.toString())
        localStorage.setItem("saved", "true")
    }

    override fun load(): BirthdayData? {
        if (localStorage.getItem("saved") != "true") return null
        val year = localStorage.getItem("year")?.toIntOrNull() ?: return null
        return BirthdayData(
            year = year,
            month = localStorage.getItem("month")?.toIntOrNull() ?: 1,
            day = localStorage.getItem("day")?.toIntOrNull() ?: 1,
            hour = localStorage.getItem("hour")?.toIntOrNull() ?: 0,
            minute = localStorage.getItem("minute")?.toIntOrNull() ?: 0
        )
    }

    override fun clear() {
        listOf("year", "month", "day", "hour", "minute", "saved", "onboarding_completed", "unknown_time")
            .forEach { localStorage.removeItem(it) }
    }

    override fun isOnboardingCompleted(): Boolean =
        localStorage.getItem("onboarding_completed") == "true"

    override fun setOnboardingCompleted(value: Boolean) {
        localStorage.setItem("onboarding_completed", if (value) "true" else "false")
    }

    override fun isUnknownTime(): Boolean =
        localStorage.getItem("unknown_time") == "true"

    override fun setUnknownTime(value: Boolean) {
        localStorage.setItem("unknown_time", if (value) "true" else "false")
    }

    override fun getLastSeenMilestoneId(): String? =
        localStorage.getItem("last_seen_milestone_id")

    override fun setLastSeenMilestoneId(id: String) {
        localStorage.setItem("last_seen_milestone_id", id)
    }
}

actual fun createBirthdayStorage(): BirthdayStorage = WebBirthdayStorage()
