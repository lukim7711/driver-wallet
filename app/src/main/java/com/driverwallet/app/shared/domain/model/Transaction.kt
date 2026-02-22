package com.driverwallet.app.shared.domain.model

import com.driverwallet.app.core.model.Category
import com.driverwallet.app.core.model.TransactionType

/**
 * Core transaction domain model.
 * No Compose dependency — data class is auto-stable by Compose compiler plugin.
 */
data class Transaction(
    val id: String = "",
    val type: TransactionType = TransactionType.INCOME,
    val category: Category? = null,
    val amount: Long = 0L,
    val note: String = "",
    val debtId: String? = null,
    val createdAt: String = "",
)
