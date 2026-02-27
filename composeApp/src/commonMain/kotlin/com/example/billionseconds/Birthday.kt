package com.example.billionseconds

import kotlinx.serialization.Serializable

@Serializable
public data class Birthday(
    public val birthDate: String,
    public val birthTime: String
) {
    public companion object {
        public fun mock(): Birthday = Birthday(
            birthDate = "2000-01-01",
            birthTime = "12:00:00"
        )

        public fun mocks(): List<Birthday> = listOf(
            mock(),
            Birthday(
                birthDate = "1990-05-15",
                birthTime = "08:30:00"
            ),
            Birthday(
                birthDate = "1985-12-25",
                birthTime = "00:00:00"
            )
        )
    }
}
