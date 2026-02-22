package com.driverwallet.app.shared.domain.model

/**
 * Summary of today's financial activity.
 * Pure domain model — no framework dependencies.
 */
data class TodaySummary(
    val income: Long = 0L,
    val expense: Long = 0L,
    val debtPayment: Long = 0L,
) {
    val profit: Long get() = income - expense - debtPayment
}
