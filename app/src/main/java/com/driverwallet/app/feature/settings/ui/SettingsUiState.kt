package com.driverwallet.app.feature.settings.ui

import androidx.compose.runtime.Immutable

@Immutable
data class FixedExpenseDisplay(
    val id: Long,
    val name: String,
    val icon: String,
    val amount: Long,
)

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val targetDate: String = "",
    val monthlyExpenses: List<FixedExpenseDisplay> = emptyList(),
    val dailyExpenses: List<FixedExpenseDisplay> = emptyList(),
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
    data class DeleteExpense(val id: Long) : SettingsUiAction
}
