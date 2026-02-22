package com.driverwallet.app.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driverwallet.app.feature.dashboard.domain.usecase.GetDashboardSummaryUseCase
import com.driverwallet.app.shared.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardSummary: GetDashboardSummaryUseCase,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeAndRefresh()
    }

    fun onAction(action: DashboardUiAction) {
        when (action) {
            is DashboardUiAction.Refresh -> {
                viewModelScope.launch { loadDashboard() }
            }
        }
    }

    /**
     * Observes today's transactions as a change signal.
     * Each time a transaction is added/updated/deleted, the Flow re-emits
     * and we reload the full dashboard data.
     *
     * collectLatest cancels any in-flight loadDashboard() when a new
     * emission arrives, preventing stale data races.
     */
    private fun observeAndRefresh() {
        viewModelScope.launch {
            transactionRepository.observeTodayTransactions()
                .collectLatest {
                    loadDashboard()
                }
        }
    }

    private suspend fun loadDashboard() {
        // Don't show loading spinner on auto-refresh if we already have data.
        // This prevents the UI from flashing Loading → Success on every change.
        val isFirstLoad = _uiState.value !is DashboardUiState.Success
        if (isFirstLoad) {
            _uiState.value = DashboardUiState.Loading
        }

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
                // On auto-refresh failure with existing data, keep showing old data.
                // Only show error on first load.
                if (isFirstLoad) {
                    _uiState.value = DashboardUiState.Error(
                        error.message ?: "Gagal memuat data",
                    )
                }
            }
    }
}
