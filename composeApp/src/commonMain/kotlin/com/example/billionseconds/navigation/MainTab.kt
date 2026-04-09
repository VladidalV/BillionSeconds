package com.example.billionseconds.navigation

enum class MainTab(
    val index: Int,
    val label: String,
    val analyticsName: String
) {
    Home(0,        "Главная",    "home"),
    Stats(1,       "Статистика", "stats"),
    Family(2,      "Семейный",   "family"),
    Milestones(3,  "Достижения", "milestones"),
    Profile(4,     "Профиль",    "profile");

    companion object {
        val default = Home
    }
}
