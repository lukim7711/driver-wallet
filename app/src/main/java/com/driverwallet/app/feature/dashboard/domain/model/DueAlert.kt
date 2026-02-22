package com.driverwallet.app.feature.dashboard.domain.model

import androidx.compose.runtime.Immutable
import com.driverwallet.app.core.model.UrgencyLevel

@Immutable
data class DueAlert(
    val debtId: String,
    val debtName: String,
    val platform: String,
    val dueDate: String,
    val amount: Long,
    val installmentNumber: Int,
    val urgency: UrgencyLevel,
)
