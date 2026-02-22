package com.driverwallet.app.feature.report.domain.model

import androidx.compose.runtime.Immutable
import com.driverwallet.app.shared.domain.model.DailySummary

@Immutable
data class WeeklyReport(
    val startDate: String,
    val endDate: String,
    val dailySummaries: List<DailySummary>,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
) {
    val totalProfit: Long get() = totalIncome - totalExpense
}
