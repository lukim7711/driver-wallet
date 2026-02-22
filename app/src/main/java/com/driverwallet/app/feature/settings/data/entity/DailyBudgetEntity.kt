package com.driverwallet.app.feature.settings.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_budgets",
    indices = [Index(value = ["category"], unique = true)],
)
data class DailyBudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val amount: Long,
)
