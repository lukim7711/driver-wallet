package com.driverwallet.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Temporary placeholder entity so Room KSP can process @Database.
 * Remove in Phase 1 when real entities (Transaction, Debt) are added.
 */
@Entity(tableName = "_placeholder")
data class PlaceholderEntity(
    @PrimaryKey val id: Int = 0,
)
