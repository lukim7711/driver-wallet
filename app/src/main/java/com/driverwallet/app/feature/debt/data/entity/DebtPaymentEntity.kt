package com.driverwallet.app.feature.debt.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Universal payment record for PERSONAL and TAB debts.
 * INSTALLMENT debts use DebtScheduleEntity for payment tracking.
 *
 * Each DebtPaymentEntity also triggers creation of a Transaction
 * (type=EXPENSE, source=DEBT_PAYMENT) for dashboard/report consistency.
 */
@Entity(
    tableName = "debt_payments",
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
data class DebtPaymentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "debt_id") val debtId: String,
    val amount: Long,
    val note: String = "",
    @ColumnInfo(name = "paid_at") val paidAt: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
)
