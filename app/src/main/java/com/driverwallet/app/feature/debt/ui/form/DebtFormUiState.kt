package com.driverwallet.app.feature.debt.ui.form

import com.driverwallet.app.feature.debt.domain.model.DebtType
import com.driverwallet.app.feature.debt.domain.model.PenaltyType

sealed interface DebtFormUiState {
    data object Loading : DebtFormUiState

    /** Step 1: Choose debt type */
    data object TypeSelection : DebtFormUiState

    /** Step 2: Fill form for selected type */
    sealed interface Ready : DebtFormUiState {
        val name: String
        val totalAmount: String
        val note: String
        val isSaving: Boolean
        val errors: Map<String, String>
        val canSave: Boolean

        /** Cicilan platform (Shopee, Kredivo, dll) */
        data class Installment(
            override val name: String = "",
            val platform: String = "Shopee",
            override val totalAmount: String = "",
            val installmentPerMonth: String = "",
            val installmentCount: String = "",
            val dueDay: String = "",
            val interestRate: String = "0",
            val penaltyType: PenaltyType = PenaltyType.NONE,
            val penaltyRate: String = "0",
            override val note: String = "",
            override val isSaving: Boolean = false,
            override val errors: Map<String, String> = emptyMap(),
        ) : Ready {
            override val canSave: Boolean
                get() = name.isNotBlank() &&
                    (totalAmount.toLongOrNull() ?: 0) > 0 &&
                    (installmentPerMonth.toLongOrNull() ?: 0) > 0 &&
                    (installmentCount.toIntOrNull() ?: 0) > 0 &&
                    (dueDay.toIntOrNull() ?: 0) in 1..31 &&
                    !isSaving
        }

        /** Hutang pribadi ke orang */
        data class Personal(
            override val name: String = "",
            val borrowerName: String = "",
            val relationship: String = "",
            val agreedReturnDate: String = "",
            override val totalAmount: String = "",
            override val note: String = "",
            override val isSaving: Boolean = false,
            override val errors: Map<String, String> = emptyMap(),
        ) : Ready {
            override val canSave: Boolean
                get() = name.isNotBlank() &&
                    borrowerName.isNotBlank() &&
                    (totalAmount.toLongOrNull() ?: 0) > 0 &&
                    !isSaving
        }

        /** Kasbon / tab di warung, bengkel */
        data class Tab(
            override val name: String = "",
            val merchantName: String = "",
            val merchantType: String = "",
            override val totalAmount: String = "",
            override val note: String = "",
            override val isSaving: Boolean = false,
            override val errors: Map<String, String> = emptyMap(),
        ) : Ready {
            override val canSave: Boolean
                get() = name.isNotBlank() &&
                    merchantName.isNotBlank() &&
                    (totalAmount.toLongOrNull() ?: 0) > 0 &&
                    !isSaving
        }
    }
}

sealed interface DebtFormUiAction {
    // --- Type selection ---
    data class SelectType(val type: DebtType) : DebtFormUiAction
    data object BackToTypeSelection : DebtFormUiAction

    // --- Universal fields ---
    data class UpdateName(val value: String) : DebtFormUiAction
    data class UpdateTotalAmount(val value: String) : DebtFormUiAction
    data class UpdateNote(val value: String) : DebtFormUiAction
    data object Save : DebtFormUiAction

    // --- Installment-specific ---
    data class UpdatePlatform(val value: String) : DebtFormUiAction
    data class UpdateInstallmentPerMonth(val value: String) : DebtFormUiAction
    data class UpdateInstallmentCount(val value: String) : DebtFormUiAction
    data class UpdateDueDay(val value: String) : DebtFormUiAction
    data class UpdateInterestRate(val value: String) : DebtFormUiAction
    data class UpdatePenaltyType(val value: PenaltyType) : DebtFormUiAction
    data class UpdatePenaltyRate(val value: String) : DebtFormUiAction

    // --- Personal-specific ---
    data class UpdateBorrowerName(val value: String) : DebtFormUiAction
    data class UpdateRelationship(val value: String) : DebtFormUiAction
    data class UpdateAgreedReturnDate(val value: String) : DebtFormUiAction

    // --- Tab-specific ---
    data class UpdateMerchantName(val value: String) : DebtFormUiAction
    data class UpdateMerchantType(val value: String) : DebtFormUiAction
}
