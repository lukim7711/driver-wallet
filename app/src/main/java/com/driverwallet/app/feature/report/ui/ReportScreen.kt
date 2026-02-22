package com.driverwallet.app.feature.report.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driverwallet.app.core.ui.component.LoadingIndicator
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.feature.report.ui.component.BarChartView
import com.driverwallet.app.feature.report.ui.component.CategoryBreakdownList
import com.driverwallet.app.feature.report.ui.component.DailyDetailList
import com.driverwallet.app.feature.report.ui.component.ReportProfitCard
import com.driverwallet.app.feature.report.ui.component.ReportTabRow
import com.driverwallet.app.feature.report.ui.component.SummaryCards
import com.driverwallet.app.feature.report.ui.component.WeekNavigator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is GlobalUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.exportUri.collect { uri ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Laporan"))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAction(ReportUiAction.Export) },
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Export CSV")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ReportTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.onAction(ReportUiAction.SelectTab(it)) },
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (uiState.selectedTab) {
                ReportTab.WEEKLY -> WeeklyContent(
                    state = uiState.weeklyState,
                    onPrevious = { viewModel.onAction(ReportUiAction.PreviousWeek) },
                    onNext = { viewModel.onAction(ReportUiAction.NextWeek) },
                )
                ReportTab.MONTHLY -> MonthlyContent(state = uiState.monthlyState)
                ReportTab.CUSTOM -> {
                    CustomContent(
                        state = uiState.customState,
                        onSelectRange = { showDatePicker = true },
                    )
                }
            }
        }
    }

    // Date Range Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMillis = datePickerState.selectedStartDateMillis
                        val endMillis = datePickerState.selectedEndDateMillis
                        if (startMillis != null && endMillis != null) {
                            val start = Instant.ofEpochMilli(startMillis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            val end = Instant.ofEpochMilli(endMillis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.onAction(ReportUiAction.SelectCustomRange(start, end))
                        }
                        showDatePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            },
        ) {
            DateRangePicker(state = datePickerState)
        }
    }
}

@Composable
private fun WeeklyContent(
    state: WeeklyState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    when (state) {
        is WeeklyState.Loading -> LoadingIndicator()
        is WeeklyState.Error -> ErrorMessage(state.message)
        is WeeklyState.Success -> {
            val report = state.report
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    WeekNavigator(
                        startDate = report.startDate,
                        endDate = report.endDate,
                        onPrevious = onPrevious,
                        onNext = onNext,
                    )
                }
                item {
                    ReportProfitCard(profit = report.totalProfit)
                }
                item {
                    BarChartView(dailySummaries = report.dailySummaries)
                }
                item {
                    SummaryCards(
                        totalIncome = report.totalIncome,
                        totalExpense = report.totalExpense,
                    )
                }
                item {
                    DailyDetailList(dailySummaries = report.dailySummaries)
                }
            }
        }
    }
}

@Composable
private fun MonthlyContent(state: MonthlyState) {
    when (state) {
        is MonthlyState.Loading -> LoadingIndicator()
        is MonthlyState.Error -> ErrorMessage(state.message)
        is MonthlyState.Success -> {
            val report = state.report
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    ReportProfitCard(
                        profit = report.totalProfit,
                        label = "Keuntungan Bulan Ini",
                    )
                }
                item {
                    SummaryCards(
                        totalIncome = report.totalIncome,
                        totalExpense = report.totalExpense,
                    )
                }
                item {
                    CategoryBreakdownList(
                        title = "Pemasukan per Kategori",
                        categories = report.incomeByCategory,
                    )
                }
                item {
                    CategoryBreakdownList(
                        title = "Pengeluaran per Kategori",
                        categories = report.expenseByCategory,
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomContent(
    state: CustomState,
    onSelectRange: () -> Unit,
) {
    when (state) {
        is CustomState.Idle -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pilih rentang tanggal",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onSelectRange) {
                        Text("Pilih Tanggal")
                    }
                }
            }
        }
        is CustomState.Loading -> LoadingIndicator()
        is CustomState.Error -> ErrorMessage(state.message)
        is CustomState.Success -> {
            val report = state.report
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    ReportProfitCard(
                        profit = report.totalProfit,
                        label = "${report.startDate} \u2014 ${report.endDate}",
                    )
                }
                item {
                    SummaryCards(
                        totalIncome = report.totalIncome,
                        totalExpense = report.totalExpense,
                    )
                }
                if (report.dailySummaries.size <= 31) {
                    item {
                        BarChartView(dailySummaries = report.dailySummaries)
                    }
                }
                item {
                    DailyDetailList(dailySummaries = report.dailySummaries)
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
