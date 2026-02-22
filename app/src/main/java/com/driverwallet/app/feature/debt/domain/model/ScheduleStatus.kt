package com.driverwallet.app.feature.debt.domain.model

/**
 * Status pembayaran satu jadwal cicilan.
 */
enum class ScheduleStatus(val value: String) {
    UNPAID("unpaid"),
    PAID("paid"),
    OVERDUE("overdue"),
    ;

    companion object {
        fun fromValue(value: String): ScheduleStatus =
            entries.find { it.value == value } ?: UNPAID
    }
}
