package com.driverwallet.app.feature.debt.domain.model

/**
 * Status lifecycle sebuah hutang.
 */
enum class DebtStatus(val value: String) {
    ACTIVE("active"),
    COMPLETED("completed"),
    ;

    companion object {
        fun fromValue(value: String): DebtStatus =
            entries.find { it.value == value } ?: ACTIVE
    }
}
