package com.driverwallet.app.feature.debt.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Kasbon/tab addition entry.
 * When a driver adds more debt at a warung/bengkel,
 * this entry is created AND the parent debt's totalAmount + remainingAmount increase.
 */
@Entity(
    tableName = "kasbon_entries",
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
data class KasbonEntryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "debt_id") val debtId: String,
    val amount: Long,
    val note: String = "",
    @ColumnInfo(name = "created_at") val createdAt: String,
)
