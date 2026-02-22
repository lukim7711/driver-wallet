package com.driverwallet.app.shared.domain.model

/**
 * Aggregated spending per category for reports.
 * Pure domain model — no framework dependencies.
 */
data class CategorySummary(
    val categoryKey: String,
    val categoryLabel: String,
    val total: Long,
    val count: Int,
    val percentage: Float = 0f,
)
