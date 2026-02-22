package com.driverwallet.app.feature.debt.domain.model

/**
 * Domain model for a debt record.
 * No Room annotations — pure Kotlin.
 */
data class Debt(
    val id: String,
    val platform: String,
    val name: String = "",
    val totalAmount: Long,
    val remainingAmount: Long,
    val installmentPerMonth: Long,
    val installmentCount: Int,
    val dueDay: Int,
    val interestRate: Double = 0.0,
    val penaltyType: PenaltyType = PenaltyType.NONE,
    val penaltyRate: Double = 0.0,
    val debtType: DebtType = DebtType.INSTALLMENT,
    val note: String = "",
    val status: DebtStatus = DebtStatus.ACTIVE,
    val startDate: String,
    val createdAt: String,
    val updatedAt: String,
) {
    val isCompleted: Boolean get() = status == DebtStatus.COMPLETED
    val isActive: Boolean get() = status == DebtStatus.ACTIVE
}
