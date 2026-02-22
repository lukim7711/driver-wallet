package com.driverwallet.app.feature.debt.domain.model

/**
 * Domain model for upcoming due schedule info.
 * Replaces UpcomingDueTuple which lived in the DAO layer.
 */
data class UpcomingDue(
    val debtId: String,
    val debtName: String,
    val platform: String,
    val dueDate: String,
    val expectedAmount: Long,
    val installmentNumber: Int,
)
