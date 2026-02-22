package com.driverwallet.app.shared.domain.model

import androidx.compose.runtime.Immutable
import com.driverwallet.app.core.model.Category
import com.driverwallet.app.core.model.TransactionType

@Immutable
data class Transaction(
    val id: String = "",
    val type: TransactionType = TransactionType.INCOME,
    val category: Category? = null,
    val amount: Long = 0L,
    val note: String = "",
    val debtId: String? = null,
    val createdAt: String = "",
)
