package com.driverwallet.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.driverwallet.app.feature.debt.data.dao.DebtDao
import com.driverwallet.app.feature.debt.data.dao.DebtScheduleDao
import com.driverwallet.app.feature.debt.data.entity.DebtEntity
import com.driverwallet.app.feature.debt.data.entity.DebtScheduleEntity
import com.driverwallet.app.feature.settings.data.dao.RecurringExpenseDao
import com.driverwallet.app.feature.settings.data.dao.SettingsDao
import com.driverwallet.app.feature.settings.data.entity.RecurringExpenseEntity
import com.driverwallet.app.feature.settings.data.entity.SettingsEntity
import com.driverwallet.app.shared.data.dao.TransactionDao
import com.driverwallet.app.shared.data.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        DebtEntity::class,
        DebtScheduleEntity::class,
        RecurringExpenseEntity::class,
        SettingsEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun debtDao(): DebtDao
    abstract fun debtScheduleDao(): DebtScheduleDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun settingsDao(): SettingsDao
}
