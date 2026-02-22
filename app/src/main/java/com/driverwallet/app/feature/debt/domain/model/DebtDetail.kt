package com.driverwallet.app.feature.debt.domain.model

/**
 * Type-specific detail for each debt variant.
 * Entity stays flat (nullable columns); this sealed interface is domain-only.
 *
 * Mapping strategy:
 * - Entity.debt_type = "installment" → DebtDetail.Installment
 * - Entity.debt_type = "personal"    → DebtDetail.Personal
 * - Entity.debt_type = "tab"         → DebtDetail.Tab
 */
sealed interface DebtDetail {

    /**
     * Cicilan platform tetap (Shopee PayLater, Kredivo, Akulaku, dll).
     * Punya jadwal cicilan bulanan yang di-generate saat create.
     */
    data class Installment(
        val platform: String,
        val installmentPerMonth: Long,
        val installmentCount: Int,
        val dueDay: Int,
        val interestRate: Double = 0.0,
        val penaltyType: PenaltyType = PenaltyType.NONE,
        val penaltyRate: Double = 0.0,
    ) : DebtDetail

    /**
     * Hutang pribadi ke orang (teman, saudara, kenalan).
     * Tidak ada jadwal tetap — bayar kapan bisa, jumlah flexible.
     */
    data class Personal(
        val borrowerName: String,
        val relationship: String = "",
        val agreedReturnDate: String? = null,
    ) : DebtDetail

    /**
     * Kasbon / tab di warung, bengkel, dll.
     * Saldo bisa NAIK (tambah kasbon) dan TURUN (bayar).
     * Riwayat penambahan dilacak via KasbonEntry.
     */
    data class Tab(
        val merchantName: String,
        val merchantType: String = "",
    ) : DebtDetail
}
