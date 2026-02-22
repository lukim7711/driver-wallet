package com.driverwallet.app.feature.report.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driverwallet.app.core.model.todayJakarta
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.feature.report.domain.usecase.ExportCsvUseCase
import com.driverwallet.app.feature.report.domain.usecase.GetCustomReportUseCase
import com.driverwallet.app.feature.report.domain.usecase.GetMonthlyReportUseCase
import com.driverwallet.app.feature.report.domain.usecase.GetWeeklyReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getWeeklyReport: GetWeeklyReportUseCase,
    private val getMonthlyReport: GetMonthlyReportUseCase,
    private val getCustomReport: GetCustomReportUseCase,
    private val exportCsv: ExportCsvUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<GlobalUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _exportUri = MutableSharedFlow<Uri>()
    val exportUri = _exportUri.asSharedFlow()

    private var currentWeekStart: LocalDate =
        todayJakarta().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    private var currentMonth: Int = todayJakarta().monthValue
    private var currentYear: Int = todayJakarta().year

    init {
        loadWeeklyReport()
        loadMonthlyReport()
    }

    fun onAction(action: ReportUiAction) {
        when (action) {
            is ReportUiAction.SelectTab -> {
                _uiState.update { it.copy(selectedTab = action.tab) }
            }
            is ReportUiAction.PreviousWeek -> {
                currentWeekStart = currentWeekStart.minusWeeks(1)
                loadWeeklyReport()
            }
            is ReportUiAction.NextWeek -> {
                currentWeekStart = currentWeekStart.plusWeeks(1)
                loadWeeklyReport()
            }
            is ReportUiAction.SelectCustomRange -> {
                loadCustomReport(action.start, action.end)
            }
            is ReportUiAction.Export -> exportReport()
        }
    }

    private fun loadWeeklyReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(weeklyState = WeeklyState.Loading) }
            runCatching { getWeeklyReport(currentWeekStart) }
                .onSuccess { report ->
                    _uiState.update { it.copy(weeklyState = WeeklyState.Success(report)) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(weeklyState = WeeklyState.Error(error.message ?: "Gagal memuat"))
                    }
                }
        }
    }

    private fun loadMonthlyReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(monthlyState = MonthlyState.Loading) }
            runCatching { getMonthlyReport(currentYear, currentMonth) }
                .onSuccess { report ->
                    _uiState.update { it.copy(monthlyState = MonthlyState.Success(report)) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(monthlyState = MonthlyState.Error(error.message ?: "Gagal memuat"))
                    }
                }
        }
    }

    private fun loadCustomReport(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(customState = CustomState.Loading) }
            runCatching { getCustomReport(start, end) }
                .onSuccess { report ->
                    _uiState.update { it.copy(customState = CustomState.Success(report)) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(customState = CustomState.Error(error.message ?: "Gagal memuat"))
                    }
                }
        }
    }

    private fun exportReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val (start, end) = when (_uiState.value.selectedTab) {
                ReportTab.WEEKLY -> {
                    val ws = (_uiState.value.weeklyState as? WeeklyState.Success)
                    (ws?.report?.startDate ?: currentWeekStart) to
                        (ws?.report?.endDate ?: currentWeekStart.plusDays(6))
                }
                ReportTab.MONTHLY -> {
                    val first = LocalDate.of(currentYear, currentMonth, 1)
                    first to first.plusMonths(1).minusDays(1)
                }
                ReportTab.CUSTOM -> {
                    val cs = (_uiState.value.customState as? CustomState.Success)
                    (cs?.report?.startDate ?: todayJakarta()) to
                        (cs?.report?.endDate ?: todayJakarta())
                }
            }
            exportCsv(start, end)
                .onSuccess { uri ->
                    _exportUri.emit(uri)
                    _uiEvent.emit(GlobalUiEvent.ShowSnackbar("CSV berhasil di-export"))
                }
                .onFailure { error ->
                    _uiEvent.emit(GlobalUiEvent.ShowSnackbar(error.message ?: "Gagal export"))
                }
            _uiState.update { it.copy(isExporting = false) }
        }
    }
}
