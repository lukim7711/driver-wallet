package com.driverwallet.app.feature.dashboard.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class DailyTarget(
    val targetAmount: Long = 0L,
    val earnedAmount: Long = 0L,
    val isRestDay: Boolean = false,
) {
    val percentage: Float
        get() = if (targetAmount > 0) (earnedAmount.toFloat() / targetAmount).coerceIn(0f, 2f) else 0f

    val isOnTrack: Boolean
        get() = isRestDay || earnedAmount >= targetAmount
}
