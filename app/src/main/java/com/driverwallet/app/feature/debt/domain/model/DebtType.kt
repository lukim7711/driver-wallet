package com.driverwallet.app.feature.debt.domain.model

/**
 * Jenis hutang yang didukung aplikasi.
 *
 * - INSTALLMENT: Cicilan platform (Shopee PayLater, Kredivo, dll)
 * - PERSONAL: Hutang pribadi (teman, saudara) — flexible payment
 * - TAB: Kasbon/tab (warung, bengkel) — saldo bisa naik
 */
enum class DebtType(val value: String) {
    INSTALLMENT("installment"),
    PERSONAL("personal"),
    TAB("tab"),
    ;

    companion object {
        fun fromValue(value: String): DebtType =
            entries.find { it.value == value } ?: INSTALLMENT
    }
}
