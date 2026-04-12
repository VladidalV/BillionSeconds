package com.example.billionseconds.navigation

enum class MainTab(
    val index: Int,
    val label: String,
    val analyticsName: String,
    val navLabel: String,
    val navIcon: String
) {
    Home(0,        "Главная",    "home",       "PULSE",    "◎"),
    Stats(1,       "Статистика", "stats",      "DATA",     "▦"),
    Family(2,      "Семейный",   "family",     "FAMILY",   "✦"),
    Milestones(3,  "Достижения", "milestones", "CAPSULES", "◈"),
    Profile(4,     "Профиль",    "profile",    "USER",     "○");

    companion object {
        val default = Home
    }
}
