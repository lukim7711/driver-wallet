package com.driverwallet.app.feature.debt.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debt_schedules",
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debt_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("debt_id")],
)
data class DebtScheduleEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "debt_id") val debtId: String,
    @ColumnInfo(name = "installment_number") val installmentNumber: Int,
    @ColumnInfo(name = "due_date") val dueDate: String,
    @ColumnInfo(name = "expected_amount") val expectedAmount: Long,
    @ColumnInfo(name = "actual_amount") val actualAmount: Long? = null,
    val status: String = "unpaid",
    @ColumnInfo(name = "paid_at") val paidAt: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
)
