package com.driverwallet.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.driverwallet.app.feature.debt.data.dao.DebtDao
import com.driverwallet.app.feature.debt.data.dao.DebtScheduleDao
import com.driverwallet.app.feature.settings.data.dao.DailyBudgetDao
import com.driverwallet.app.feature.settings.data.dao.DailyExpenseDao
import com.driverwallet.app.feature.settings.data.dao.MonthlyExpenseDao
import com.driverwallet.app.feature.settings.data.dao.RecurringExpenseDao
import com.driverwallet.app.feature.settings.data.dao.SettingsDao
import com.driverwallet.app.shared.data.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_debts_status_is_deleted` ON `debts` (`status`, `is_deleted`)"
        )
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE transactions ADD COLUMN source TEXT NOT NULL DEFAULT 'manual'"
        )
        db.execSQL(
            "UPDATE transactions SET source = 'debt_payment' WHERE debt_id IS NOT NULL"
        )
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create unified recurring_expenses table
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `recurring_expenses` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `icon` TEXT NOT NULL DEFAULT 'payments',
                `amount` INTEGER NOT NULL,
                `frequency` TEXT NOT NULL,
                `is_deleted` INTEGER NOT NULL DEFAULT 0
            )""".trimIndent()
        )

        // Migrate daily_budgets → recurring_expenses (frequency = 'daily')
        db.execSQL(
            """INSERT INTO `recurring_expenses` (`name`, `icon`, `amount`, `frequency`, `is_deleted`)
            SELECT `category`, 'payments', `amount`, 'daily', 0
            FROM `daily_budgets`
            WHERE `amount` > 0""".trimIndent()
        )

        // Migrate daily_expenses → recurring_expenses (frequency = 'daily')
        db.execSQL(
            """INSERT INTO `recurring_expenses` (`name`, `icon`, `amount`, `frequency`, `is_deleted`)
            SELECT `name`, `icon`, `amount`, 'daily', `is_deleted`
            FROM `daily_expenses`""".trimIndent()
        )

        // Migrate monthly_expenses → recurring_expenses (frequency = 'monthly')
        db.execSQL(
            """INSERT INTO `recurring_expenses` (`name`, `icon`, `amount`, `frequency`, `is_deleted`)
            SELECT `name`, `icon`, `amount`, 'monthly', `is_deleted`
            FROM `monthly_expenses`""".trimIndent()
        )

        // Old tables kept temporarily — will be dropped in cleanup commit
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "driver_wallet.db",
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .addCallback(DatabaseCallback())
            .build()

    @Provides
    @Singleton
    fun provideTransactionRunner(database: AppDatabase): TransactionRunner =
        RoomTransactionRunner(database)

    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideDebtDao(db: AppDatabase): DebtDao = db.debtDao()
    @Provides fun provideDebtScheduleDao(db: AppDatabase): DebtScheduleDao = db.debtScheduleDao()
    @Provides fun provideRecurringExpenseDao(db: AppDatabase): RecurringExpenseDao = db.recurringExpenseDao()
    @Provides fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()

    // Legacy providers — kept temporarily for backward compat
    @Suppress("DEPRECATION")
    @Provides fun provideDailyBudgetDao(db: AppDatabase): DailyBudgetDao = db.dailyBudgetDao()
    @Suppress("DEPRECATION")
    @Provides fun provideMonthlyExpenseDao(db: AppDatabase): MonthlyExpenseDao = db.monthlyExpenseDao()
    @Suppress("DEPRECATION")
    @Provides fun provideDailyExpenseDao(db: AppDatabase): DailyExpenseDao = db.dailyExpenseDao()
}
