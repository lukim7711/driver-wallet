package com.driverwallet.app.feature.debt.domain.model

/**
 * Universal payment record for non-installment debts (PERSONAL, TAB).
 *
 * For INSTALLMENT debts, payments are tracked via DebtSchedule.
 * For PERSONAL and TAB debts, payments are tracked via DebtPayment
 * because there's no fixed schedule.
 *
 * Each payment also creates a Transaction (type=EXPENSE, source=DEBT_PAYMENT)
 * for dashboard/report consistency.
 */
data class DebtPayment(
    val id: String,
    val debtId: String,
    val amount: Long,
    val note: String = "",
    val paidAt: String,
    val createdAt: String,
)
