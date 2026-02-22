package com.driverwallet.app.feature.dashboard.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class BudgetInfo(
    val totalBudget: Long = 0L,
    val spentToday: Long = 0L,
) {
    val remaining: Long get() = (totalBudget - spentToday).coerceAtLeast(0)

    val percentage: Float
        get() = if (totalBudget > 0) (spentToday.toFloat() / totalBudget).coerceIn(0f, 1f) else 0f
}
