package com.driverwallet.app.feature.debt.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driverwallet.app.core.model.todayJakarta
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.feature.debt.domain.model.DebtType
import com.driverwallet.app.feature.debt.domain.usecase.DebtFormParams
import com.driverwallet.app.feature.debt.domain.usecase.SaveDebtUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DebtFormViewModel @Inject constructor(
    private val saveDebtUseCase: SaveDebtUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DebtFormUiState>(DebtFormUiState.TypeSelection)
    val uiState: StateFlow<DebtFormUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<GlobalUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _savedSuccessfully = MutableSharedFlow<Unit>()
    val savedSuccessfully = _savedSuccessfully.asSharedFlow()

    fun onAction(action: DebtFormUiAction) {
        when (action) {
            is DebtFormUiAction.SelectType -> selectType(action.type)
            is DebtFormUiAction.BackToTypeSelection -> _uiState.value = DebtFormUiState.TypeSelection
            is DebtFormUiAction.Save -> save()
            else -> updateField(action)
        }
    }

    // ==================== Type Selection ====================

    private fun selectType(type: DebtType) {
        _uiState.value = when (type) {
            DebtType.INSTALLMENT -> DebtFormUiState.Ready.Installment()
            DebtType.PERSONAL -> DebtFormUiState.Ready.Personal()
            DebtType.TAB -> DebtFormUiState.Ready.Tab()
        }
    }

    // ==================== Field Updates ====================

    private fun updateField(action: DebtFormUiAction) {
        val current = _uiState.value as? DebtFormUiState.Ready ?: return
        _uiState.value = when (current) {
            is DebtFormUiState.Ready.Installment -> updateInstallmentField(current, action)
            is DebtFormUiState.Ready.Personal -> updatePersonalField(current, action)
            is DebtFormUiState.Ready.Tab -> updateTabField(current, action)
        }
    }

    private fun updateInstallmentField(
        state: DebtFormUiState.Ready.Installment,
        action: DebtFormUiAction,
    ): DebtFormUiState.Ready = when (action) {
        is DebtFormUiAction.UpdateName -> state.copy(name = action.value)
        is DebtFormUiAction.UpdatePlatform -> state.copy(platform = action.value)
        is DebtFormUiAction.UpdateTotalAmount -> state.copy(totalAmount = action.value.filter { it.isDigit() })
        is DebtFormUiAction.UpdateInstallmentPerMonth -> state.copy(installmentPerMonth = action.value.filter { it.isDigit() })
        is DebtFormUiAction.UpdateInstallmentCount -> state.copy(installmentCount = action.value.filter { it.isDigit() })
        is DebtFormUiAction.UpdateDueDay -> state.copy(dueDay = action.value.filter { it.isDigit() })
        is DebtFormUiAction.UpdateInterestRate -> state.copy(interestRate = action.value)
        is DebtFormUiAction.UpdatePenaltyType -> state.copy(penaltyType = action.value)
        is DebtFormUiAction.UpdatePenaltyRate -> state.copy(penaltyRate = action.value)
        is DebtFormUiAction.UpdateNote -> state.copy(note = action.value)
        else -> state
    }

    private fun updatePersonalField(
        state: DebtFormUiState.Ready.Personal,
        action: DebtFormUiAction,
    ): DebtFormUiState.Ready = when (action) {
        is DebtFormUiAction.UpdateName -> state.copy(name = action.value)
        is DebtFormUiAction.UpdateTotalAmount -> state.copy(totalAmount = action.value.filter { it.isDigit() })
        is DebtFormUiAction.UpdateBorrowerName -> state.copy(borrowerName = action.value)
        is DebtFormUiAction.UpdateRelationship -> state.copy(relationship = action.value)
        is DebtFormUiAction.UpdateAgreedReturnDate -> state.copy(agreedReturnDate = action.value)
        is DebtFormUiAction.UpdateNote -> state.copy(note = action.value)
        else -> state
    }

    private fun updateTabField(
        state: DebtFormUiState.Ready.Tab,
        action: DebtFormUiAction,
    ): DebtFormUiState.Ready = when (action) {
        is DebtFormUiAction.UpdateName -> state.copy(name = action.value)
        is DebtFormUiAction.UpdateTotalAmount -> state.copy(totalAmount = action.value.filter { it.isDigit() })
        is DebtFormUiAction.UpdateMerchantName -> state.copy(merchantName = action.value)
        is DebtFormUiAction.UpdateMerchantType -> state.copy(merchantType = action.value)
        is DebtFormUiAction.UpdateNote -> state.copy(note = action.value)
        else -> state
    }

    // ==================== Save ====================

    private fun save() {
        val current = _uiState.value as? DebtFormUiState.Ready ?: return
        if (!current.canSave) return

        viewModelScope.launch {
            setSaving(true)
            val params = buildParams(current)
            saveDebtUseCase(params)
                .onSuccess {
                    _uiEvent.emit(GlobalUiEvent.ShowSnackbar("Hutang \"${params.name}\" berhasil disimpan"))
                    _savedSuccessfully.emit(Unit)
                }
                .onFailure { error ->
                    setSaving(false)
                    setErrors(mapOf("general" to (error.message ?: "Gagal menyimpan")))
                    _uiEvent.emit(GlobalUiEvent.ShowSnackbar(error.message ?: "Gagal menyimpan"))
                }
        }
    }

    private fun buildParams(state: DebtFormUiState.Ready): DebtFormParams = when (state) {
        is DebtFormUiState.Ready.Installment -> DebtFormParams.Installment(
            name = state.name.trim(),
            platform = state.platform,
            totalAmount = state.totalAmount.toLongOrNull() ?: 0L,
            installmentPerMonth = state.installmentPerMonth.toLongOrNull() ?: 0L,
            installmentCount = state.installmentCount.toIntOrNull() ?: 0,
            dueDay = state.dueDay.toIntOrNull() ?: 1,
            interestRate = state.interestRate.toDoubleOrNull() ?: 0.0,
            penaltyType = state.penaltyType,
            penaltyRate = state.penaltyRate.toDoubleOrNull() ?: 0.0,
            note = state.note,
            startDate = todayJakarta(),
        )
        is DebtFormUiState.Ready.Personal -> DebtFormParams.Personal(
            name = state.name.trim(),
            borrowerName = state.borrowerName.trim(),
            relationship = state.relationship,
            agreedReturnDate = state.agreedReturnDate.takeIf { it.isNotBlank() }
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            totalAmount = state.totalAmount.toLongOrNull() ?: 0L,
            note = state.note,
            startDate = todayJakarta(),
        )
        is DebtFormUiState.Ready.Tab -> DebtFormParams.Tab(
            name = state.name.trim(),
            merchantName = state.merchantName.trim(),
            merchantType = state.merchantType,
            totalAmount = state.totalAmount.toLongOrNull() ?: 0L,
            note = state.note,
            startDate = todayJakarta(),
        )
    }

    // ==================== UI state helpers ====================

    private fun setSaving(saving: Boolean) {
        val current = _uiState.value as? DebtFormUiState.Ready ?: return
        _uiState.value = when (current) {
            is DebtFormUiState.Ready.Installment -> current.copy(isSaving = saving)
            is DebtFormUiState.Ready.Personal -> current.copy(isSaving = saving)
            is DebtFormUiState.Ready.Tab -> current.copy(isSaving = saving)
        }
    }

    private fun setErrors(errors: Map<String, String>) {
        val current = _uiState.value as? DebtFormUiState.Ready ?: return
        _uiState.value = when (current) {
            is DebtFormUiState.Ready.Installment -> current.copy(errors = errors)
            is DebtFormUiState.Ready.Personal -> current.copy(errors = errors)
            is DebtFormUiState.Ready.Tab -> current.copy(errors = errors)
        }
    }
}
