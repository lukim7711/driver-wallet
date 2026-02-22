package com.driverwallet.app.feature.settings.domain.model

/**
 * Domain model for fixed daily expense.
 * No Room annotations — pure Kotlin.
 */
data class DailyExpense(
    val id: Long = 0,
    val name: String,
    val icon: String = "payments",
    val amount: Long,
)
