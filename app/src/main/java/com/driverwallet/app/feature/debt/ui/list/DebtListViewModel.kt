package com.driverwallet.app.feature.debt.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.core.util.CurrencyFormatter
import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.debt.domain.model.ScheduleStatus
import com.driverwallet.app.feature.debt.domain.usecase.GetActiveDebtsUseCase
import com.driverwallet.app.feature.debt.domain.usecase.PayDebtInstallmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtListViewModel @Inject constructor(
    private val getActiveDebts: GetActiveDebtsUseCase,
    private val payInstallment: PayDebtInstallmentUseCase,
    private val debtRepository: DebtRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DebtListUiState>(DebtListUiState.Loading)
    val uiState: StateFlow<DebtListUiState> = _uiState.asStateFlow()

    private val _paymentDialog = MutableStateFlow<PaymentDialogState?>(null)
    val paymentDialog: StateFlow<PaymentDialogState?> = _paymentDialog.asStateFlow()

    private val _uiEvent = Channel<GlobalUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        observeDebts()
    }

    private fun observeDebts() {
        viewModelScope.launch {
            getActiveDebts().collect { debts ->
                if (debts.isEmpty()) {
                    _uiState.value = DebtListUiState.Empty
                } else {
                    val totalRemaining = debtRepository.observeTotalRemaining().first()
                    val hasOverdue = debts.any { info ->
                        info.nextSchedule?.status == ScheduleStatus.OVERDUE
                    }
                    _uiState.value = DebtListUiState.Success(
                        totalRemaining = totalRemaining,
                        debts = debts,
                        hasOverdue = hasOverdue,
                    )
                }
            }
        }
    }

    fun onAction(action: DebtListUiAction) {
        when (action) {
            is DebtListUiAction.OpenPayment -> {
                _paymentDialog.value = PaymentDialogState(
                    debtId = action.debtId,
                    scheduleId = action.schedule.id,
                    debtName = "",
                    installmentNumber = action.schedule.installmentNumber,
                    expectedAmount = action.schedule.expectedAmount,
                    payAmount = action.schedule.expectedAmount,
                )
            }
            is DebtListUiAction.DismissPayment -> {
                _paymentDialog.value = null
            }
            is DebtListUiAction.ConfirmPayment -> confirmPayment(action)
            is DebtListUiAction.DeleteDebt -> deleteDebt(action.debtId)
        }
    }

    private fun confirmPayment(action: DebtListUiAction.ConfirmPayment) {
        viewModelScope.launch {
            _paymentDialog.value = _paymentDialog.value?.copy(isProcessing = true)
            payInstallment(action.debtId, action.scheduleId, action.amount)
                .onSuccess {
                    _paymentDialog.value = null
                    _uiEvent.send(
                        GlobalUiEvent.ShowSnackbar(
                            "Cicilan Rp ${CurrencyFormatter.format(action.amount)} berhasil dibayar",
                        ),
                    )
                }
                .onFailure { error ->
                    _paymentDialog.value = _paymentDialog.value?.copy(isProcessing = false)
                    _uiEvent.send(
                        GlobalUiEvent.ShowSnackbar(error.message ?: "Gagal membayar cicilan"),
                    )
                }
        }
    }

    private fun deleteDebt(debtId: String) {
        viewModelScope.launch {
            runCatching { debtRepository.softDelete(debtId) }
                .onSuccess {
                    _uiEvent.send(GlobalUiEvent.ShowSnackbar("Hutang dihapus"))
                }
                .onFailure { error ->
                    _uiEvent.send(GlobalUiEvent.ShowSnackbar(error.message ?: "Gagal menghapus"))
                }
        }
    }
}
