package com.driverwallet.app.core.model

enum class UrgencyLevel {
    OVERDUE,
    CRITICAL,
    WARNING,
    NORMAL;

    companion object {
        fun fromDaysUntilDue(days: Long): UrgencyLevel = when {
            days < 0 -> OVERDUE
            days <= 2 -> CRITICAL
            days <= 7 -> WARNING
            else -> NORMAL
        }
    }
}
