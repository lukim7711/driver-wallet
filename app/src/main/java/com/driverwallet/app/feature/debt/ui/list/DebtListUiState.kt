package com.driverwallet.app.feature.debt.ui.list

import com.driverwallet.app.feature.debt.domain.DebtWithScheduleInfo
import com.driverwallet.app.feature.debt.domain.model.DebtSchedule
import com.driverwallet.app.feature.debt.domain.model.KasbonEntry

sealed interface DebtListUiState {
    data object Loading : DebtListUiState

    data class Success(
        val totalRemaining: Long,
        val debts: List<DebtWithScheduleInfo>,
        val hasOverdue: Boolean,
    ) : DebtListUiState

    data object Empty : DebtListUiState
}

sealed interface DebtListUiAction {
    // --- Installment payment (schedule-based) ---
    data class OpenPayment(val debtId: String, val schedule: DebtSchedule) : DebtListUiAction
    data object DismissPayment : DebtListUiAction
    data class ConfirmPayment(val debtId: String, val scheduleId: String, val amount: Long) : DebtListUiAction

    // --- Flexible payment (PERSONAL / TAB) ---
    data class OpenPayDebt(val debtId: String, val debtName: String, val remainingAmount: Long) : DebtListUiAction
    data object DismissPayDebt : DebtListUiAction
    data class ConfirmPayDebt(val debtId: String, val amount: Long, val note: String) : DebtListUiAction

    // --- Kasbon (TAB only) ---
    data class OpenKasbonHistory(val debtId: String) : DebtListUiAction
    data object DismissKasbonHistory : DebtListUiAction
    data class OpenAddKasbon(val debtId: String) : DebtListUiAction
    data object DismissAddKasbon : DebtListUiAction
    data class ConfirmAddKasbon(val debtId: String, val amount: Long, val note: String) : DebtListUiAction

    // --- Delete ---
    data class DeleteDebt(val debtId: String) : DebtListUiAction
}

/** Installment payment dialog state */
data class PaymentDialogState(
    val debtId: String = "",
    val scheduleId: String = "",
    val debtName: String = "",
    val installmentNumber: Int = 0,
    val expectedAmount: Long = 0L,
    val payAmount: Long = 0L,
    val isProcessing: Boolean = false,
)

/** Flexible payment dialog state (PERSONAL / TAB) */
data class FlexiblePaymentDialogState(
    val debtId: String = "",
    val debtName: String = "",
    val remainingAmount: Long = 0L,
    val isProcessing: Boolean = false,
)

/** Kasbon history sheet state */
data class KasbonHistoryState(
    val debtId: String = "",
    val entries: List<KasbonEntry> = emptyList(),
)
