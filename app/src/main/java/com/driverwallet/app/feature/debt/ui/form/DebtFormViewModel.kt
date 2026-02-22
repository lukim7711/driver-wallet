package com.driverwallet.app.feature.debt.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driverwallet.app.core.model.todayJakarta
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.feature.debt.domain.usecase.DebtFormParams
import com.driverwallet.app.feature.debt.domain.usecase.SaveDebtUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtFormViewModel @Inject constructor(
    private val saveDebtUseCase: SaveDebtUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DebtFormUiState>(DebtFormUiState.Ready())
    val uiState: StateFlow<DebtFormUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<GlobalUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _savedSuccessfully = MutableSharedFlow<Unit>()
    val savedSuccessfully = _savedSuccessfully.asSharedFlow()

    fun onAction(action: DebtFormUiAction) {
        val current = _uiState.value as? DebtFormUiState.Ready ?: return
        when (action) {
            is DebtFormUiAction.UpdateName -> _uiState.value = current.copy(name = action.value)
            is DebtFormUiAction.UpdatePlatform -> _uiState.value = current.copy(platform = action.value)
            is DebtFormUiAction.UpdateTotalAmount -> _uiState.value = current.copy(totalAmount = action.value.filter { it.isDigit() })
            is DebtFormUiAction.UpdateInstallmentPerMonth -> _uiState.value = current.copy(installmentPerMonth = action.value.filter { it.isDigit() })
            is DebtFormUiAction.UpdateInstallmentCount -> _uiState.value = current.copy(installmentCount = action.value.filter { it.isDigit() })
            is DebtFormUiAction.UpdateDueDay -> _uiState.value = current.copy(dueDay = action.value.filter { it.isDigit() })
            is DebtFormUiAction.UpdateInterestRate -> _uiState.value = current.copy(interestRate = action.value)
            is DebtFormUiAction.UpdatePenaltyType -> _uiState.value = current.copy(penaltyType = action.value)
            is DebtFormUiAction.UpdatePenaltyRate -> _uiState.value = current.copy(penaltyRate = action.value)
            is DebtFormUiAction.UpdateNote -> _uiState.value = current.copy(note = action.value)
            is DebtFormUiAction.Save -> save(current)
        }
    }

    private fun save(current: DebtFormUiState.Ready) {
        if (!current.canSave) return
        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true)
            val params = DebtFormParams(
                name = current.name.trim(),
                platform = current.platform,
                totalAmount = current.totalAmount.toLongOrNull() ?: 0L,
                installmentPerMonth = current.installmentPerMonth.toLongOrNull() ?: 0L,
                installmentCount = current.installmentCount.toIntOrNull() ?: 0,
                dueDay = current.dueDay.toIntOrNull() ?: 1,
                interestRate = current.interestRate.toDoubleOrNull() ?: 0.0,
                penaltyType = current.penaltyType,
                penaltyRate = current.penaltyRate.toDoubleOrNull() ?: 0.0,
                note = current.note,
                startDate = todayJakarta(),
            )
            saveDebtUseCase(params)
                .onSuccess {
                    _uiEvent.emit(GlobalUiEvent.ShowSnackbar("Hutang \"${params.name}\" berhasil disimpan"))
                    _savedSuccessfully.emit(Unit)
                }
                .onFailure { error ->
                    _uiState.value = current.copy(
                        isSaving = false,
                        errors = mapOf("general" to (error.message ?: "Gagal menyimpan")),
                    )
                    _uiEvent.emit(GlobalUiEvent.ShowSnackbar(error.message ?: "Gagal menyimpan"))
                }
        }
    }
}
