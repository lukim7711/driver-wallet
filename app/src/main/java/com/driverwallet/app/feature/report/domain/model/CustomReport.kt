package com.driverwallet.app.feature.report.domain.model

import androidx.compose.runtime.Immutable
import com.driverwallet.app.shared.domain.model.DailySummary

@Immutable
data class CustomReport(
    val startDate: String,
    val endDate: String,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val dailySummaries: List<DailySummary> = emptyList(),
) {
    val totalProfit: Long get() = totalIncome - totalExpense
}
