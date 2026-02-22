package com.driverwallet.app.feature.settings.domain.model

enum class RecurringFrequency(val value: String) {
    DAILY("daily"),
    MONTHLY("monthly");

    companion object {
        fun fromValue(value: String): RecurringFrequency =
            entries.first { it.value == value }
    }
}
