package com.driverwallet.app.feature.report.ui

import com.driverwallet.app.feature.report.domain.model.CustomReport
import com.driverwallet.app.feature.report.domain.model.MonthlyReport
import com.driverwallet.app.feature.report.domain.model.WeeklyReport
import java.time.LocalDate

enum class ReportTab { WEEKLY, MONTHLY, CUSTOM }

data class ReportUiState(
    val selectedTab: ReportTab = ReportTab.WEEKLY,
    val weeklyState: WeeklyState = WeeklyState.Loading,
    val monthlyState: MonthlyState = MonthlyState.Loading,
    val customState: CustomState = CustomState.Idle,
    val isExporting: Boolean = false,
)

sealed interface WeeklyState {
    data object Loading : WeeklyState
    data class Success(val report: WeeklyReport) : WeeklyState
    data class Error(val message: String) : WeeklyState
}

sealed interface MonthlyState {
    data object Loading : MonthlyState
    data class Success(val report: MonthlyReport) : MonthlyState
    data class Error(val message: String) : MonthlyState
}

sealed interface CustomState {
    data object Idle : CustomState
    data object Loading : CustomState
    data class Success(val report: CustomReport) : CustomState
    data class Error(val message: String) : CustomState
}

sealed interface ReportUiAction {
    data class SelectTab(val tab: ReportTab) : ReportUiAction
    data object PreviousWeek : ReportUiAction
    data object NextWeek : ReportUiAction
    data class SelectCustomRange(val start: LocalDate, val end: LocalDate) : ReportUiAction
    data object Export : ReportUiAction
}
