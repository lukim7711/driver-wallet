package com.driverwallet.app.core.model

/**
 * Asal transaksi.
 * MANUAL = user input via QuickInput screen
 * DEBT_PAYMENT = auto-generated saat bayar cicilan hutang
 */
enum class TransactionSource(val value: String) {
    MANUAL("manual"),
    DEBT_PAYMENT("debt_payment"),
    ;

    companion object {
        fun fromValue(value: String): TransactionSource =
            entries.find { it.value == value } ?: MANUAL
    }
}
