package com.driverwallet.app.shared.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.driverwallet.app.feature.debt.data.entity.DebtEntity

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debt_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("created_at"),
        Index("type"),
        Index("category"),
        Index("debt_id"),
        Index("is_deleted"),
    ],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String,
    val category: String,
    val amount: Long,
    val note: String = "",
    @ColumnInfo(name = "debt_id") val debtId: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
)
