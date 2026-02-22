package com.driverwallet.app.feature.debt.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debts",
    indices = [
        Index(value = ["status", "is_deleted"]),
        Index(value = ["debt_type"]),
    ],
)
data class DebtEntity(
    @PrimaryKey val id: String,

    // --- Universal fields ---
    val name: String = "",
    @ColumnInfo(name = "debt_type") val debtType: String = "installment",
    @ColumnInfo(name = "total_amount") val totalAmount: Long,
    @ColumnInfo(name = "remaining_amount") val remainingAmount: Long,
    val note: String = "",
    val status: String = "active",
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,

    // --- INSTALLMENT-specific (kept non-null with defaults for backward compat) ---
    val platform: String = "",
    @ColumnInfo(name = "installment_per_month") val installmentPerMonth: Long = 0L,
    @ColumnInfo(name = "installment_count") val installmentCount: Int = 0,
    @ColumnInfo(name = "due_day") val dueDay: Int = 0,
    @ColumnInfo(name = "interest_rate") val interestRate: Double = 0.0,
    @ColumnInfo(name = "penalty_type") val penaltyType: String = "none",
    @ColumnInfo(name = "penalty_rate") val penaltyRate: Double = 0.0,

    // --- PERSONAL-specific (nullable, added in MIGRATION_5_6) ---
    @ColumnInfo(name = "borrower_name") val borrowerName: String? = null,
    val relationship: String? = null,
    @ColumnInfo(name = "agreed_return_date") val agreedReturnDate: String? = null,

    // --- TAB-specific (nullable, added in MIGRATION_5_6) ---
    @ColumnInfo(name = "merchant_name") val merchantName: String? = null,
    @ColumnInfo(name = "merchant_type") val merchantType: String? = null,
)
