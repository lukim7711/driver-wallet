package com.driverwallet.app.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.feature.settings.domain.SettingsKeys
import com.driverwallet.app.feature.settings.domain.SettingsRepository
import com.driverwallet.app.feature.settings.domain.usecase.SaveDailyBudgetsUseCase
import com.driverwallet.app.feature.settings.domain.usecase.SaveDailyExpenseUseCase
import com.driverwallet.app.feature.settings.domain.usecase.SaveMonthlyExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val saveDailyBudgets: SaveDailyBudgetsUseCase,
    private val saveMonthlyExpense: SaveMonthlyExpenseUseCase,
    private val saveDailyExpense: SaveDailyExpenseUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<GlobalUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        observeDarkMode()
        observeTargetDate()
        observeBudgets()
        observeMonthlyExpenses()
        observeDailyExpenses()
    }

    private fun observeDarkMode() {
        viewModelScope.launch {
            settingsRepository.observeSetting(SettingsKeys.DARK_MODE).collect { value ->
                _uiState.update { it.copy(isDarkMode = value == "true") }
            }
        }
    }

    private fun observeTargetDate() {
        viewModelScope.launch {
            settingsRepository.observeSetting(SettingsKeys.DEBT_TARGET_DATE).collect { value ->
                _uiState.update { it.copy(targetDate = value ?: "") }
            }
        }
    }

    private fun observeBudgets() {
        viewModelScope.launch {
            settingsRepository.observeDailyBudgets().collect { budgets ->
                val map = budgets.associate { it.category to it.amount.toString() }
                _uiState.update {
                    it.copy(
                        budgetBbm = map["fuel"] ?: "0",
                        budgetMakan = map["food"] ?: "0",
                        budgetRokok = map["cigarette"] ?: "0",
                        budgetPulsa = map["phone"] ?: "0",
                    )
                }
            }
        }
    }

    private fun observeMonthlyExpenses() {
        viewModelScope.launch {
            settingsRepository.observeMonthlyExpenses().collect { expenses ->
                _uiState.update {
                    it.copy(
                        monthlyExpenses = expenses.map { expense ->
                            FixedExpenseDisplay(
                                id = expense.id,
                                name = expense.name,
                                icon = expense.icon,
                                amount = expense.amount,
                            )
                        },
                    )
                }
            }
        }
    }

    private fun observeDailyExpenses() {
        viewModelScope.launch {
            settingsRepository.observeDailyExpenses().collect { expenses ->
                _uiState.update {
                    it.copy(
                        dailyExpenses = expenses.map { expense ->
                            FixedExpenseDisplay(
                                id = expense.id,
                                name = expense.name,
                                icon = expense.icon,
                                amount = expense.amount,
                            )
                        },
                    )
                }
            }
        }
    }

    fun onAction(action: SettingsUiAction) {
        when (action) {
            is SettingsUiAction.ToggleDarkMode -> toggleDarkMode(action.enabled)
            is SettingsUiAction.UpdateBudget -> updateBudgetField(action.category, action.value)
            is SettingsUiAction.UpdateTargetDate -> _uiState.update { it.copy(targetDate = action.date) }
            is SettingsUiAction.SaveAll -> saveAll()
            is SettingsUiAction.ShowAddExpense -> showAddDialog(action.isMonthly)
            is SettingsUiAction.ShowEditExpense -> showEditDialog(action)
            is SettingsUiAction.DismissExpenseDialog -> dismissDialog()
            is SettingsUiAction.SaveExpense -> saveExpense(action.expense)
            is SettingsUiAction.DeleteExpense -> deleteExpense(action.id, action.isMonthly)
        }
    }

    private fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveSetting(SettingsKeys.DARK_MODE, enabled.toString())
        }
    }

    private fun updateBudgetField(category: String, value: String) {
        _uiState.update {
            when (category) {
                "fuel" -> it.copy(budgetBbm = value)
                "food" -> it.copy(budgetMakan = value)
                "cigarette" -> it.copy(budgetRokok = value)
                "phone" -> it.copy(budgetPulsa = value)
                else -> it
            }
        }
    }

    private fun saveAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value

            val budgets = mapOf(
                "fuel" to (state.budgetBbm.toLongOrNull() ?: 0L),
                "food" to (state.budgetMakan.toLongOrNull() ?: 0L),
                "cigarette" to (state.budgetRokok.toLongOrNull() ?: 0L),
                "phone" to (state.budgetPulsa.toLongOrNull() ?: 0L),
            )
            saveDailyBudgets(budgets)

            if (state.targetDate.isNotBlank()) {
                settingsRepository.saveSetting(SettingsKeys.DEBT_TARGET_DATE, state.targetDate)
            }

            _uiState.update { it.copy(isSaving = false) }
            _uiEvent.send(GlobalUiEvent.ShowSnackbar("Pengaturan berhasil disimpan"))
        }
    }

    private fun showAddDialog(isMonthly: Boolean) {
        _uiState.update {
            it.copy(
                showExpenseDialog = true,
                editingExpense = EditingExpense(isMonthly = isMonthly),
            )
        }
    }

    private fun showEditDialog(action: SettingsUiAction.ShowEditExpense) {
        _uiState.update {
            it.copy(
                showExpenseDialog = true,
                editingExpense = EditingExpense(
                    id = action.id,
                    name = action.name,
                    amount = action.amount.toString(),
                    icon = action.icon,
                    isMonthly = action.isMonthly,
                ),
            )
        }
    }

    private fun dismissDialog() {
        _uiState.update { it.copy(showExpenseDialog = false, editingExpense = null) }
    }

    private fun saveExpense(expense: EditingExpense) {
        viewModelScope.launch {
            val amount = expense.amount.toLongOrNull() ?: 0L
            val result = if (expense.isMonthly) {
                saveMonthlyExpense(
                    id = expense.id,
                    name = expense.name,
                    amount = amount,
                    icon = expense.icon,
                )
            } else {
                saveDailyExpense(
                    id = expense.id,
                    name = expense.name,
                    amount = amount,
                    icon = expense.icon,
                )
            }
            result
                .onSuccess { dismissDialog() }
                .onFailure { error ->
                    _uiEvent.send(GlobalUiEvent.ShowSnackbar(error.message ?: "Gagal menyimpan"))
                }
        }
    }

    private fun deleteExpense(id: Long, isMonthly: Boolean) {
        viewModelScope.launch {
            runCatching {
                if (isMonthly) settingsRepository.deleteMonthlyExpense(id)
                else settingsRepository.deleteDailyExpense(id)
            }.onSuccess {
                _uiEvent.send(GlobalUiEvent.ShowSnackbar("Berhasil dihapus"))
            }
        }
    }
}
