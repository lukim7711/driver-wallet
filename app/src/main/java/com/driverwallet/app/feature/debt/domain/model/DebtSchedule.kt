package com.driverwallet.app.feature.debt.domain.model

/**
 * Domain model for a debt payment schedule entry.
 * No Room annotations — pure Kotlin.
 */
data class DebtSchedule(
    val id: String,
    val debtId: String,
    val installmentNumber: Int,
    val dueDate: String,
    val expectedAmount: Long,
    val actualAmount: Long? = null,
    val status: String = "unpaid",
    val paidAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
)
