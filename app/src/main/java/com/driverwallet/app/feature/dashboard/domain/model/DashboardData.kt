package com.driverwallet.app.feature.dashboard.domain.model

import androidx.compose.runtime.Immutable
import com.driverwallet.app.shared.domain.model.Transaction

@Immutable
data class DashboardData(
    val todaySummary: TodaySummary = TodaySummary(),
    val dailyTarget: DailyTarget = DailyTarget(),
    val budgetInfo: BudgetInfo = BudgetInfo(),
    val dueAlerts: List<DueAlert> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val yesterdayProfit: Long? = null,
)
