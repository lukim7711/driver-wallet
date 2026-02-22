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
    val status: ScheduleStatus = ScheduleStatus.UNPAID,
    val paidAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
) {
    val isPaid: Boolean get() = status == ScheduleStatus.PAID
    val isOverdue: Boolean get() = status == ScheduleStatus.OVERDUE
}
