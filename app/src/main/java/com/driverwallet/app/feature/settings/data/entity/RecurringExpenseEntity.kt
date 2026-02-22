package com.driverwallet.app.feature.settings.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "payments",
    val amount: Long,
    val frequency: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
)
