package com.driverwallet.app.feature.input.ui

import com.driverwallet.app.core.model.Category
import com.driverwallet.app.core.model.TransactionType

sealed interface QuickInputUiState {
    data object Loading : QuickInputUiState

    data class Ready(
        val type: TransactionType = TransactionType.INCOME,
        val amount: Long = 0L,
        val displayAmount: String = "0",
        val selectedCategory: Category? = null,
        val categories: List<Category> = emptyList(),
        val note: String = "",
        val isSaving: Boolean = false,
    ) : QuickInputUiState {
        val canSave: Boolean get() = amount > 0 && selectedCategory != null && !isSaving
    }
}
