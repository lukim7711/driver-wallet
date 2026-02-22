package com.driverwallet.app.feature.report.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class DailySummary(
    val date: String,
    val income: Long = 0L,
    val expense: Long = 0L,
    val transactionCount: Int = 0,
) {
    val profit: Long get() = income - expense
}
