package com.driverwallet.app.feature.debt.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.core.util.CurrencyFormatter
import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.debt.domain.model.ScheduleStatus
import com.driverwallet.app.feature.debt.domain.usecase.AddKasbonEntryUseCase
import com.driverwallet.app.feature.debt.domain.usecase.GetActiveDebtsUseCase
import com.driverwallet.app.feature.debt.domain.usecase.PayDebtInstallmentUseCase
import com.driverwallet.app.feature.debt.domain.usecase.PayDebtUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtListViewModel @Inject constructor(
    private val getActiveDebts: GetActiveDebtsUseCase,
    private val payInstallment: PayDebtInstallmentUseCase,
    private val payDebt: PayDebtUseCase,
    private val addKasbonEntry: AddKasbonEntryUseCase,
    private val debtRepository: DebtRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DebtListUiState>(DebtListUiState.Loading)
    val uiState: StateFlow<DebtListUiState> = _uiState.asStateFlow()

    // --- Installment payment ---
    private val _paymentDialog = MutableStateFlow<PaymentDialogState?>(null)
    val paymentDialog: StateFlow<PaymentDialogState?> = _paymentDialog.asStateFlow()

    // --- Flexible payment (PERSONAL / TAB) ---
    private val _flexiblePaymentDialog = MutableStateFlow<FlexiblePaymentDialogState?>(null)
    val flexiblePaymentDialog: StateFlow<FlexiblePaymentDialogState?> = _flexiblePaymentDialog.asStateFlow()

    // --- Kasbon history ---
    private val _kasbonHistoryState = MutableStateFlow<KasbonHistoryState?>(null)
    val kasbonHistoryState: StateFlow<KasbonHistoryState?> = _kasbonHistoryState.asStateFlow()

    // --- Add kasbon dialog ---
    private val _showAddKasbon = MutableStateFlow<String?>(null) // debtId or null
    val showAddKasbon: StateFlow<String?> = _showAddKasbon.asStateFlow()

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
            // --- Installment payment ---
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
            is DebtListUiAction.DismissPayment -> _paymentDialog.value = null
            is DebtListUiAction.ConfirmPayment -> confirmInstallmentPayment(action)

            // --- Flexible payment ---
            is DebtListUiAction.OpenPayDebt -> {
                _flexiblePaymentDialog.value = FlexiblePaymentDialogState(
                    debtId = action.debtId,
                    debtName = action.debtName,
                    remainingAmount = action.remainingAmount,
                )
            }
            is DebtListUiAction.DismissPayDebt -> _flexiblePaymentDialog.value = null
            is DebtListUiAction.ConfirmPayDebt -> confirmFlexiblePayment(action)

            // --- Kasbon ---
            is DebtListUiAction.OpenKasbonHistory -> observeKasbonHistory(action.debtId)
            is DebtListUiAction.DismissKasbonHistory -> _kasbonHistoryState.value = null
            is DebtListUiAction.OpenAddKasbon -> _showAddKasbon.value = action.debtId
            is DebtListUiAction.DismissAddKasbon -> _showAddKasbon.value = null
            is DebtListUiAction.ConfirmAddKasbon -> confirmAddKasbon(action)

            // --- Delete ---
            is DebtListUiAction.DeleteDebt -> deleteDebt(action.debtId)
        }
    }

    // ==================== Installment Payment ====================

    private fun confirmInstallmentPayment(action: DebtListUiAction.ConfirmPayment) {
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

    // ==================== Flexible Payment ====================

    private fun confirmFlexiblePayment(action: DebtListUiAction.ConfirmPayDebt) {
        viewModelScope.launch {
            _flexiblePaymentDialog.value = _flexiblePaymentDialog.value?.copy(isProcessing = true)
            payDebt(action.debtId, action.amount, action.note)
                .onSuccess {
                    _flexiblePaymentDialog.value = null
                    _uiEvent.send(
                        GlobalUiEvent.ShowSnackbar(
                            "Pembayaran Rp ${CurrencyFormatter.format(action.amount)} berhasil",
                        ),
                    )
                }
                .onFailure { error ->
                    _flexiblePaymentDialog.value = _flexiblePaymentDialog.value?.copy(isProcessing = false)
                    _uiEvent.send(
                        GlobalUiEvent.ShowSnackbar(error.message ?: "Gagal membayar"),
                    )
                }
        }
    }

    // ==================== Kasbon ====================

    private fun observeKasbonHistory(debtId: String) {
        viewModelScope.launch {
            debtRepository.observeKasbonEntries(debtId).collect { entries ->
                _kasbonHistoryState.value = KasbonHistoryState(
                    debtId = debtId,
                    entries = entries,
                )
            }
        }
    }

    private fun confirmAddKasbon(action: DebtListUiAction.ConfirmAddKasbon) {
        viewModelScope.launch {
            addKasbonEntry(action.debtId, action.amount, action.note)
                .onSuccess {
                    _showAddKasbon.value = null
                    _uiEvent.send(
                        GlobalUiEvent.ShowSnackbar(
                            "Kasbon Rp ${CurrencyFormatter.format(action.amount)} ditambahkan",
                        ),
                    )
                }
                .onFailure { error ->
                    _uiEvent.send(
                        GlobalUiEvent.ShowSnackbar(error.message ?: "Gagal menambah kasbon"),
                    )
                }
        }
    }

    // ==================== Delete ====================

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
