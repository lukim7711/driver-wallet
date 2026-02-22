package com.driverwallet.app.feature.debt.ui.form

import com.driverwallet.app.feature.debt.domain.model.PenaltyType

sealed interface DebtFormUiState {
    data object Loading : DebtFormUiState

    data class Ready(
        val name: String = "",
        val platform: String = "Shopee",
        val totalAmount: String = "",
        val installmentPerMonth: String = "",
        val installmentCount: String = "",
        val dueDay: String = "",
        val interestRate: String = "0",
        val penaltyType: PenaltyType = PenaltyType.NONE,
        val penaltyRate: String = "0",
        val note: String = "",
        val isSaving: Boolean = false,
        val errors: Map<String, String> = emptyMap(),
    ) : DebtFormUiState {
        val canSave: Boolean
            get() = name.isNotBlank() &&
                (totalAmount.toLongOrNull() ?: 0) > 0 &&
                (installmentPerMonth.toLongOrNull() ?: 0) > 0 &&
                (installmentCount.toIntOrNull() ?: 0) > 0 &&
                (dueDay.toIntOrNull() ?: 0) in 1..31 &&
                !isSaving
    }
}

sealed interface DebtFormUiAction {
    data class UpdateName(val value: String) : DebtFormUiAction
    data class UpdatePlatform(val value: String) : DebtFormUiAction
    data class UpdateTotalAmount(val value: String) : DebtFormUiAction
    data class UpdateInstallmentPerMonth(val value: String) : DebtFormUiAction
    data class UpdateInstallmentCount(val value: String) : DebtFormUiAction
    data class UpdateDueDay(val value: String) : DebtFormUiAction
    data class UpdateInterestRate(val value: String) : DebtFormUiAction
    data class UpdatePenaltyType(val value: PenaltyType) : DebtFormUiAction
    data class UpdatePenaltyRate(val value: String) : DebtFormUiAction
    data class UpdateNote(val value: String) : DebtFormUiAction
    data object Save : DebtFormUiAction
}
