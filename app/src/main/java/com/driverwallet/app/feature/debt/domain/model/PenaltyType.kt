package com.driverwallet.app.feature.debt.domain.model

/**
 * Jenis denda keterlambatan pembayaran.
 */
enum class PenaltyType(val value: String) {
    NONE("none"),
    FIXED("fixed"),
    PERCENTAGE("percentage"),
    ;

    companion object {
        fun fromValue(value: String): PenaltyType =
            entries.find { it.value == value } ?: NONE
    }
}
