package com.driverwallet.app.feature.debt.domain.model

/**
 * A single kasbon/tab addition entry.
 * When a driver adds more debt at a warung/bengkel, a KasbonEntry is created.
 * This INCREASES the debt's totalAmount and remainingAmount.
 *
 * Payments (decreasing remaining) go through DebtPayment or DebtSchedule.
 */
data class KasbonEntry(
    val id: String,
    val debtId: String,
    val amount: Long,
    val note: String = "",
    val createdAt: String,
)
