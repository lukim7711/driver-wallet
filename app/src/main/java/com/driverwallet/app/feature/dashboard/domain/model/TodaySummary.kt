package com.driverwallet.app.feature.dashboard.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class TodaySummary(
    val income: Long = 0L,
    val expense: Long = 0L,
    val debtPayment: Long = 0L,
) {
    val profit: Long get() = income - expense - debtPayment
}
