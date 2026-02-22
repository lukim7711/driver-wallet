package com.driverwallet.app.feature.dashboard.ui

import androidx.compose.runtime.Immutable
import com.driverwallet.app.feature.dashboard.domain.model.BudgetInfo
import com.driverwallet.app.feature.dashboard.domain.model.DailyTarget
import com.driverwallet.app.feature.dashboard.domain.model.DueAlert
import com.driverwallet.app.feature.dashboard.domain.model.TodaySummary
import com.driverwallet.app.shared.domain.model.Transaction

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    /**
     * @Immutable tells Compose compiler to skip recomposition when
     * the reference hasn't changed. Required because [List] fields
     * are structurally unstable (could be mutable lists).
     *
     * Note: @Immutable on UI state ≠ @Immutable on domain models.
     * Domain models should be framework-free; UI state can use Compose annotations.
     */
    @Immutable
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
