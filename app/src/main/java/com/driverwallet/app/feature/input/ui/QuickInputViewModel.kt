package com.driverwallet.app.feature.input.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driverwallet.app.core.model.Categories
import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.core.util.CurrencyFormatter
import com.driverwallet.app.feature.input.domain.SaveTransactionUseCase
import com.driverwallet.app.shared.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickInputViewModel @Inject constructor(
    private val saveTransactionUseCase: SaveTransactionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuickInputUiState>(
        QuickInputUiState.Ready(
            type = TransactionType.INCOME,
            categories = Categories.incomeCategories,
        ),
    )
    val uiState: StateFlow<QuickInputUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<GlobalUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var amountString = ""

    fun onAction(action: QuickInputUiAction) {
        val current = _uiState.value as? QuickInputUiState.Ready ?: return
        when (action) {
            is QuickInputUiAction.SwitchType -> switchType(action.type, current)
            is QuickInputUiAction.SelectCategory -> selectCategory(action.category, current)
            is QuickInputUiAction.AppendDigit -> appendDigit(action.digit, current)
            is QuickInputUiAction.Backspace -> backspace(current)
            is QuickInputUiAction.AddPreset -> addPreset(action.amount, current)
            is QuickInputUiAction.UpdateNote -> updateNote(action.note, current)
            is QuickInputUiAction.Save -> save(current)
        }
    }

    private fun switchType(type: TransactionType, current: QuickInputUiState.Ready) {
        val categories = when (type) {
            TransactionType.INCOME -> Categories.incomeCategories
            TransactionType.EXPENSE -> Categories.expenseCategories
        }
        _uiState.value = current.copy(
            type = type,
            categories = categories,
            selectedCategory = null,
        )
    }

    private fun selectCategory(category: com.driverwallet.app.core.model.Category, current: QuickInputUiState.Ready) {
        _uiState.value = current.copy(selectedCategory = category)
    }

    private fun appendDigit(digit: String, current: QuickInputUiState.Ready) {
        val newAmountString = amountString + digit
        if (newAmountString.length > SaveTransactionUseCase.MAX_DIGITS) return
        amountString = newAmountString
        updateAmount(current)
    }

    private fun backspace(current: QuickInputUiState.Ready) {
        if (amountString.isEmpty()) return
        amountString = amountString.dropLast(1)
        updateAmount(current)
    }

    private fun addPreset(presetAmount: Long, current: QuickInputUiState.Ready) {
        val newAmount = current.amount + presetAmount
        if (newAmount > SaveTransactionUseCase.MAX_AMOUNT) return
        amountString = newAmount.toString()
        updateAmount(current)
    }

    private fun updateAmount(current: QuickInputUiState.Ready) {
        val amount = amountString.toLongOrNull() ?: 0L
        _uiState.value = current.copy(
            amount = amount,
            displayAmount = if (amount == 0L) "0" else CurrencyFormatter.format(amount),
        )
    }

    private fun updateNote(note: String, current: QuickInputUiState.Ready) {
        if (note.length > SaveTransactionUseCase.MAX_NOTE_LENGTH) return
        _uiState.value = current.copy(note = note)
    }

    private fun save(current: QuickInputUiState.Ready) {
        if (!current.canSave) return
        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true)
            val transaction = Transaction(
                type = current.type,
                category = current.selectedCategory,
                amount = current.amount,
                note = current.note,
            )
            saveTransactionUseCase(transaction)
                .onSuccess {
                    val typeLabel = when (current.type) {
                        TransactionType.INCOME -> "Pemasukan"
                        TransactionType.EXPENSE -> "Pengeluaran"
                    }
                    _uiEvent.emit(
                        GlobalUiEvent.ShowSnackbar(
                            "$typeLabel Rp ${CurrencyFormatter.format(current.amount)} tersimpan",
                        ),
                    )
                    resetForm()
                }
                .onFailure { error ->
                    _uiEvent.emit(
                        GlobalUiEvent.ShowSnackbar(error.message ?: "Gagal menyimpan"),
                    )
                    _uiState.value = current.copy(isSaving = false)
                }
        }
    }

    private fun resetForm() {
        amountString = ""
        val currentState = _uiState.value as? QuickInputUiState.Ready ?: return
        _uiState.value = currentState.copy(
            amount = 0L,
            displayAmount = "0",
            selectedCategory = null,
            note = "",
            isSaving = false,
        )
    }
}
