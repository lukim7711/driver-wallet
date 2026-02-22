package com.driverwallet.app.feature.debt.domain.model

/**
 * Jenis hutang yang didukung aplikasi.
 */
enum class DebtType(val value: String) {
    INSTALLMENT("installment"),
    ;

    companion object {
        fun fromValue(value: String): DebtType =
            entries.find { it.value == value } ?: INSTALLMENT
    }
}
