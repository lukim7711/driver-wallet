package com.driverwallet.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.driverwallet.app.feature.debt.data.dao.DebtDao
import com.driverwallet.app.feature.debt.data.dao.DebtScheduleDao
import com.driverwallet.app.feature.debt.data.entity.DebtEntity
import com.driverwallet.app.feature.debt.data.entity.DebtScheduleEntity
import com.driverwallet.app.feature.settings.data.dao.DailyBudgetDao
import com.driverwallet.app.feature.settings.data.dao.DailyExpenseDao
import com.driverwallet.app.feature.settings.data.dao.MonthlyExpenseDao
import com.driverwallet.app.feature.settings.data.dao.SettingsDao
import com.driverwallet.app.feature.settings.data.entity.DailyBudgetEntity
import com.driverwallet.app.feature.settings.data.entity.DailyExpenseEntity
import com.driverwallet.app.feature.settings.data.entity.MonthlyExpenseEntity
import com.driverwallet.app.feature.settings.data.entity.SettingsEntity
import com.driverwallet.app.shared.data.dao.TransactionDao
import com.driverwallet.app.shared.data.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        DebtEntity::class,
        DebtScheduleEntity::class,
        DailyBudgetEntity::class,
        MonthlyExpenseEntity::class,
        DailyExpenseEntity::class,
        SettingsEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun debtDao(): DebtDao
    abstract fun debtScheduleDao(): DebtScheduleDao
    abstract fun dailyBudgetDao(): DailyBudgetDao
    abstract fun monthlyExpenseDao(): MonthlyExpenseDao
    abstract fun dailyExpenseDao(): DailyExpenseDao
    abstract fun settingsDao(): SettingsDao
}
