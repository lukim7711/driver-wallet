package com.driverwallet.app.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driverwallet.app.core.ui.component.LoadingIndicator
import com.driverwallet.app.feature.dashboard.ui.component.BudgetRemainingCard
import com.driverwallet.app.feature.dashboard.ui.component.DailyTargetSection
import com.driverwallet.app.feature.dashboard.ui.component.DueAlertCard
import com.driverwallet.app.feature.dashboard.ui.component.IncomeExpenseRow
import com.driverwallet.app.feature.dashboard.ui.component.ProfitHeroCard
import com.driverwallet.app.feature.dashboard.ui.component.TodayTransactionList

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is DashboardUiState.Loading -> LoadingIndicator()
        is DashboardUiState.Error -> {
            ErrorContent(
                message = state.message,
                onRetry = { viewModel.onAction(DashboardUiAction.Refresh) },
            )
        }
        is DashboardUiState.Success -> {
            DashboardContent(state = state)
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "hero") {
            ProfitHeroCard(
                profit = state.todaySummary.profit,
                percentChange = state.percentChange,
            )
        }
        item(key = "income_expense") {
            IncomeExpenseRow(
                income = state.todaySummary.income,
                expense = state.todaySummary.expense,
            )
        }
        item(key = "daily_target") {
            DailyTargetSection(dailyTarget = state.dailyTarget)
        }
        item(key = "budget") {
            BudgetRemainingCard(budgetInfo = state.budgetInfo)
        }
        if (state.dueAlerts.isNotEmpty()) {
            item(key = "due_alerts") {
                DueAlertCard(alerts = state.dueAlerts)
            }
        }
        item(key = "transactions") {
            TodayTransactionList(transactions = state.recentTransactions)
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Muat ulang")
                Text(" Coba Lagi")
            }
        }
    }
}
