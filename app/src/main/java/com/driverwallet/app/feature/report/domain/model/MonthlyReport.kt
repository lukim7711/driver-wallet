package com.driverwallet.app.feature.report.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class MonthlyReport(
    val month: String,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val categoryBreakdown: List<CategorySummary> = emptyList(),
) {
    val totalProfit: Long get() = totalIncome - totalExpense
}
