package com.driverwallet.app.feature.settings.domain.model

/**
 * Domain model for daily budget per category.
 * No Room annotations — pure Kotlin.
 */
data class DailyBudget(
    val id: Long = 0,
    val category: String,
    val amount: Long,
)
