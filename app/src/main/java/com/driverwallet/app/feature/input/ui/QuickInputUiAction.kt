package com.driverwallet.app.feature.input.ui

import com.driverwallet.app.core.model.Category
import com.driverwallet.app.core.model.TransactionType

sealed interface QuickInputUiAction {
    data class SwitchType(val type: TransactionType) : QuickInputUiAction
    data class SelectCategory(val category: Category) : QuickInputUiAction
    data class AppendDigit(val digit: String) : QuickInputUiAction
    data object Backspace : QuickInputUiAction
    data class AddPreset(val amount: Long) : QuickInputUiAction
    data class UpdateNote(val note: String) : QuickInputUiAction
    data object Save : QuickInputUiAction
}
