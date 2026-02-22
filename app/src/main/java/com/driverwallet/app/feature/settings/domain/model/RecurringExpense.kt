package com.driverwallet.app.feature.settings.domain.model

import androidx.compose.runtime.Immutable

/**
 * Unified domain model for all recurring expenses.
 * Replaces DailyBudget, DailyExpense, and MonthlyExpense.
 */
@Immutable
data class RecurringExpense(
    val id: Long = 0,
    val name: String,
    val icon: String = "payments",
    val amount: Long,
    val frequency: RecurringFrequency,
) {
    val isDaily: Boolean get() = frequency == RecurringFrequency.DAILY
    val isMonthly: Boolean get() = frequency == RecurringFrequency.MONTHLY
}
