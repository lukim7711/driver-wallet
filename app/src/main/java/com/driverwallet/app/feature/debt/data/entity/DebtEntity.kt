package com.driverwallet.app.feature.debt.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debts",
    indices = [
        Index(value = ["status", "is_deleted"]),
    ],
)
data class DebtEntity(
    @PrimaryKey val id: String,
    val platform: String,
    val name: String = "",
    @ColumnInfo(name = "total_amount") val totalAmount: Long,
    @ColumnInfo(name = "remaining_amount") val remainingAmount: Long,
    @ColumnInfo(name = "installment_per_month") val installmentPerMonth: Long,
    @ColumnInfo(name = "installment_count") val installmentCount: Int,
    @ColumnInfo(name = "due_day") val dueDay: Int,
    @ColumnInfo(name = "interest_rate") val interestRate: Double = 0.0,
    @ColumnInfo(name = "penalty_type") val penaltyType: String = "none",
    @ColumnInfo(name = "penalty_rate") val penaltyRate: Double = 0.0,
    @ColumnInfo(name = "debt_type") val debtType: String = "installment",
    val note: String = "",
    val status: String = "active",
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
)
