package com.example.billionseconds

public sealed class BirthdayEvent {
    public data class ShowError(
        public val message: String
    ) : BirthdayEvent()

    public data class ShowToast(
        public val message: String
    ) : BirthdayEvent()
}
