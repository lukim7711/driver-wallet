package com.driverwallet.app.feature.report.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class CategorySummary(
    val categoryKey: String,
    val categoryLabel: String,
    val total: Long,
    val count: Int,
    val percentage: Float = 0f,
)
