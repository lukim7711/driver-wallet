package com.driverwallet.app.feature.dashboard.ui

import com.driverwallet.app.feature.dashboard.domain.model.BudgetInfo
import com.driverwallet.app.feature.dashboard.domain.model.DailyTarget
import com.driverwallet.app.feature.dashboard.domain.model.DueAlert
import com.driverwallet.app.feature.dashboard.domain.model.TodaySummary
import com.driverwallet.app.shared.domain.model.Transaction

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Success(
        val todaySummary: TodaySummary,
        val percentChange: Float?,
        val dailyTarget: DailyTarget,
        val budgetInfo: BudgetInfo,
        val dueAlerts: List<DueAlert>,
        val recentTransactions: List<Transaction>,
    ) : DashboardUiState

    data class Error(val message: String) : DashboardUiState
}

sealed interface DashboardUiAction {
    data object Refresh : DashboardUiAction
}
