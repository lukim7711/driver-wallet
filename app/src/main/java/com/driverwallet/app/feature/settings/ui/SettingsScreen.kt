package com.driverwallet.app.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.core.ui.util.ObserveAsEvents
import com.driverwallet.app.feature.settings.ui.component.BudgetSection
import com.driverwallet.app.feature.settings.ui.component.DarkModeToggle
import com.driverwallet.app.feature.settings.ui.component.ExpenseFormDialog
import com.driverwallet.app.feature.settings.ui.component.FixedExpenseDisplay
import com.driverwallet.app.feature.settings.ui.component.FixedExpenseSection
import com.driverwallet.app.feature.settings.ui.component.TargetDateRow

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is GlobalUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Header
            item {
                Text(
                    text = "Pengaturan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Dark Mode
            item {
                DarkModeToggle(
                    isDarkMode = uiState.isDarkMode,
                    onToggle = { viewModel.onAction(SettingsUiAction.ToggleDarkMode(it)) },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Budget Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                BudgetSection(
                    budgetBbm = uiState.budgetBbm,
                    budgetMakan = uiState.budgetMakan,
                    budgetRokok = uiState.budgetRokok,
                    budgetPulsa = uiState.budgetPulsa,
                    onBudgetChange = { category, value ->
                        viewModel.onAction(SettingsUiAction.UpdateBudget(category, value))
                    },
                )
            }

            // Target Date
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TargetDateRow(
                    targetDate = uiState.targetDate,
                    onDateSelected = { viewModel.onAction(SettingsUiAction.UpdateTargetDate(it)) },
                )
            }

            // Monthly Expenses
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                FixedExpenseSection(
                    title = "Pengeluaran Tetap Bulanan",
                    expenses = uiState.monthlyExpenses.map {
                        FixedExpenseDisplay(it.id, it.name, it.icon, it.amount)
                    },
                    onAdd = { viewModel.onAction(SettingsUiAction.ShowAddExpense(isMonthly = true)) },
                    onEdit = { expense ->
                        viewModel.onAction(
                            SettingsUiAction.ShowEditExpense(
                                id = expense.id,
                                name = expense.name,
                                amount = expense.amount,
                                icon = expense.icon,
                                isMonthly = true,
                            ),
                        )
                    },
                    onDelete = { id ->
                        viewModel.onAction(SettingsUiAction.DeleteExpense(id, isMonthly = true))
                    },
                )
            }

            // Daily Expenses
            item {
                Spacer(modifier = Modifier.height(8.dp))
                FixedExpenseSection(
                    title = "Pengeluaran Tetap Harian",
                    expenses = uiState.dailyExpenses.map {
                        FixedExpenseDisplay(it.id, it.name, it.icon, it.amount)
                    },
                    onAdd = { viewModel.onAction(SettingsUiAction.ShowAddExpense(isMonthly = false)) },
                    onEdit = { expense ->
                        viewModel.onAction(
                            SettingsUiAction.ShowEditExpense(
                                id = expense.id,
                                name = expense.name,
                                amount = expense.amount,
                                icon = expense.icon,
                                isMonthly = false,
                            ),
                        )
                    },
                    onDelete = { id ->
                        viewModel.onAction(SettingsUiAction.DeleteExpense(id, isMonthly = false))
                    },
                )
            }

            // Save Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.onAction(SettingsUiAction.SaveAll) },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Simpan Perubahan")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Expense Form Dialog
    if (uiState.showExpenseDialog && uiState.editingExpense != null) {
        ExpenseFormDialog(
            expense = uiState.editingExpense!!,
            onDismiss = { viewModel.onAction(SettingsUiAction.DismissExpenseDialog) },
            onSave = { viewModel.onAction(SettingsUiAction.SaveExpense(it)) },
        )
    }
}
