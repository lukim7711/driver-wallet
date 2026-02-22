package com.driverwallet.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.driverwallet.app.feature.debt.data.dao.DebtDao
import com.driverwallet.app.feature.debt.data.dao.DebtPaymentDao
import com.driverwallet.app.feature.debt.data.dao.DebtScheduleDao
import com.driverwallet.app.feature.debt.data.dao.KasbonEntryDao
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
        db.execSQL(
            """INSERT INTO `recurring_expenses` (`name`, `icon`, `amount`, `frequency`, `is_deleted`)
            SELECT `category`, 'payments', `amount`, 'daily', 0
            FROM `daily_budgets`
            WHERE `amount` > 0""".trimIndent()
        )
        db.execSQL(
            """INSERT INTO `recurring_expenses` (`name`, `icon`, `amount`, `frequency`, `is_deleted`)
            SELECT `name`, `icon`, `amount`, 'daily', `is_deleted`
            FROM `daily_expenses`""".trimIndent()
        )
        db.execSQL(
            """INSERT INTO `recurring_expenses` (`name`, `icon`, `amount`, `frequency`, `is_deleted`)
            SELECT `name`, `icon`, `amount`, 'monthly', `is_deleted`
            FROM `monthly_expenses`""".trimIndent()
        )
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `daily_budgets`")
        db.execSQL("DROP TABLE IF EXISTS `daily_expenses`")
        db.execSQL("DROP TABLE IF EXISTS `monthly_expenses`")
    }
}

private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // --- Add PERSONAL-specific columns to debts ---
        db.execSQL("ALTER TABLE debts ADD COLUMN borrower_name TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE debts ADD COLUMN relationship TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE debts ADD COLUMN agreed_return_date TEXT DEFAULT NULL")

        // --- Add TAB-specific columns to debts ---
        db.execSQL("ALTER TABLE debts ADD COLUMN merchant_name TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE debts ADD COLUMN merchant_type TEXT DEFAULT NULL")

        // --- Add debt_type index ---
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_debts_debt_type` ON `debts` (`debt_type`)")

        // --- Create kasbon_entries table ---
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `kasbon_entries` (
                `id` TEXT NOT NULL,
                `debt_id` TEXT NOT NULL,
                `amount` INTEGER NOT NULL,
                `note` TEXT NOT NULL DEFAULT '',
                `created_at` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`debt_id`) REFERENCES `debts`(`id`) ON DELETE CASCADE
            )""".trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_kasbon_entries_debt_id` ON `kasbon_entries` (`debt_id`)")

        // --- Create debt_payments table ---
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `debt_payments` (
                `id` TEXT NOT NULL,
                `debt_id` TEXT NOT NULL,
                `amount` INTEGER NOT NULL,
                `note` TEXT NOT NULL DEFAULT '',
                `paid_at` TEXT NOT NULL,
                `created_at` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`debt_id`) REFERENCES `debts`(`id`) ON DELETE CASCADE
            )""".trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_debt_payments_debt_id` ON `debt_payments` (`debt_id`)")
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
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
            )
            .addCallback(DatabaseCallback())
            .build()

    @Provides
    @Singleton
    fun provideTransactionRunner(database: AppDatabase): TransactionRunner =
        RoomTransactionRunner(database)

    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideDebtDao(db: AppDatabase): DebtDao = db.debtDao()
    @Provides fun provideDebtScheduleDao(db: AppDatabase): DebtScheduleDao = db.debtScheduleDao()
    @Provides fun provideDebtPaymentDao(db: AppDatabase): DebtPaymentDao = db.debtPaymentDao()
    @Provides fun provideKasbonEntryDao(db: AppDatabase): KasbonEntryDao = db.kasbonEntryDao()
    @Provides fun provideRecurringExpenseDao(db: AppDatabase): RecurringExpenseDao = db.recurringExpenseDao()
    @Provides fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()
}
