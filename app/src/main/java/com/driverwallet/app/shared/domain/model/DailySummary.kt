package com.driverwallet.app.shared.domain.model

/**
 * Daily income/expense summary for reports.
 * Pure domain model — no framework dependencies.
 */
data class DailySummary(
    val date: String,
    val income: Long = 0L,
    val expense: Long = 0L,
    val transactionCount: Int = 0,
) {
    val profit: Long get() = income - expense
}
