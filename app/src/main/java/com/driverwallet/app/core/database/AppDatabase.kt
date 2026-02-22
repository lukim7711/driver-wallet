package com.driverwallet.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room database shell for Driver Wallet.
 * Entities and DAOs will be registered in Phase 1 (Issue #3).
 */
@Database(
    entities = [],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase()
