package com.driverwallet.app.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driverwallet.app.feature.dashboard.domain.usecase.GetDashboardSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardSummary: GetDashboardSummaryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun onAction(action: DashboardUiAction) {
        when (action) {
            is DashboardUiAction.Refresh -> loadDashboard()
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            runCatching { getDashboardSummary() }
                .onSuccess { data ->
                    val percentChange = data.yesterdayProfit?.let { yesterday ->
                        if (yesterday != 0L) {
                            ((data.todaySummary.profit - yesterday).toFloat() / yesterday * 100f)
                        } else {
                            null
                        }
                    }
                    _uiState.value = DashboardUiState.Success(
                        todaySummary = data.todaySummary,
                        percentChange = percentChange,
                        dailyTarget = data.dailyTarget,
                        budgetInfo = data.budgetInfo,
                        dueAlerts = data.dueAlerts,
                        recentTransactions = data.recentTransactions,
                    )
                }
                .onFailure { error ->
                    _uiState.value = DashboardUiState.Error(
                        error.message ?: "Gagal memuat data",
                    )
                }
        }
    }
}
