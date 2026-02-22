package com.driverwallet.app.feature.settings.ui

import com.driverwallet.app.feature.settings.data.entity.DailyExpenseEntity
import com.driverwallet.app.feature.settings.data.entity.MonthlyExpenseEntity

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val budgetBbm: String = "",
    val budgetMakan: String = "",
    val budgetRokok: String = "",
    val budgetPulsa: String = "",
    val targetDate: String = "",
    val monthlyExpenses: List<MonthlyExpenseEntity> = emptyList(),
    val dailyExpenses: List<DailyExpenseEntity> = emptyList(),
    val isSaving: Boolean = false,
    val showExpenseDialog: Boolean = false,
    val editingExpense: EditingExpense? = null,
)

data class EditingExpense(
    val id: Long = 0,
    val name: String = "",
    val amount: String = "",
    val icon: String = "payments",
    val isMonthly: Boolean = true,
)

sealed interface SettingsUiAction {
    data class ToggleDarkMode(val enabled: Boolean) : SettingsUiAction
    data class UpdateBudget(val category: String, val value: String) : SettingsUiAction
    data class UpdateTargetDate(val date: String) : SettingsUiAction
    data object SaveAll : SettingsUiAction
    data class ShowAddExpense(val isMonthly: Boolean) : SettingsUiAction
    data class ShowEditExpense(
        val id: Long,
        val name: String,
        val amount: Long,
        val icon: String,
        val isMonthly: Boolean,
    ) : SettingsUiAction
    data object DismissExpenseDialog : SettingsUiAction
    data class SaveExpense(val expense: EditingExpense) : SettingsUiAction
    data class DeleteExpense(val id: Long, val isMonthly: Boolean) : SettingsUiAction
}
