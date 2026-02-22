package com.driverwallet.app.feature.debt.ui.list

import com.driverwallet.app.feature.debt.data.entity.DebtScheduleEntity
import com.driverwallet.app.feature.debt.domain.DebtWithScheduleInfo

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
    data class OpenPayment(val debtId: String, val schedule: DebtScheduleEntity) : DebtListUiAction
    data object DismissPayment : DebtListUiAction
    data class ConfirmPayment(val debtId: String, val scheduleId: String, val amount: Long) : DebtListUiAction
    data class DeleteDebt(val debtId: String) : DebtListUiAction
}

data class PaymentDialogState(
    val debtId: String = "",
    val scheduleId: String = "",
    val debtName: String = "",
    val installmentNumber: Int = 0,
    val expectedAmount: Long = 0L,
    val payAmount: Long = 0L,
    val isProcessing: Boolean = false,
)
