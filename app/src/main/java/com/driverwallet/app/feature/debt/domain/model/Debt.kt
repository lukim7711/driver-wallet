package com.driverwallet.app.feature.debt.domain.model

/**
 * Domain model for a debt record.
 * No Room annotations — pure Kotlin.
 *
 * Universal fields live at top level.
 * Type-specific fields live inside [detail] sealed interface.
 *
 * Backward-compatible computed aliases provided for consumers
 * that still reference flat installment fields.
 */
data class Debt(
    val id: String,
    val name: String = "",
    val debtType: DebtType = DebtType.INSTALLMENT,
    val detail: DebtDetail,
    val totalAmount: Long,
    val remainingAmount: Long,
    val note: String = "",
    val status: DebtStatus = DebtStatus.ACTIVE,
    val startDate: String,
    val createdAt: String,
    val updatedAt: String,
) {
    // --- Status helpers ---
    val isCompleted: Boolean get() = status == DebtStatus.COMPLETED
    val isActive: Boolean get() = status == DebtStatus.ACTIVE

    // --- Progress ---
    val paidAmount: Long get() = totalAmount - remainingAmount
    val progressPercent: Float
        get() = if (totalAmount > 0) paidAmount.toFloat() / totalAmount else 0f

    // --- Backward-compatible aliases for INSTALLMENT consumers ---
    val platform: String
        get() = when (val d = detail) {
            is DebtDetail.Installment -> d.platform
            is DebtDetail.Personal -> d.borrowerName
            is DebtDetail.Tab -> d.merchantName
        }

    val installmentPerMonth: Long
        get() = (detail as? DebtDetail.Installment)?.installmentPerMonth ?: 0L

    val installmentCount: Int
        get() = (detail as? DebtDetail.Installment)?.installmentCount ?: 0

    val dueDay: Int
        get() = (detail as? DebtDetail.Installment)?.dueDay ?: 0

    val interestRate: Double
        get() = (detail as? DebtDetail.Installment)?.interestRate ?: 0.0

    val penaltyType: PenaltyType
        get() = (detail as? DebtDetail.Installment)?.penaltyType ?: PenaltyType.NONE

    val penaltyRate: Double
        get() = (detail as? DebtDetail.Installment)?.penaltyRate ?: 0.0
}
