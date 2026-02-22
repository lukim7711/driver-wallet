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
        // Bug #11: Add composite index for debt list queries
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_debts_status_is_deleted` ON `debts` (`status`, `is_deleted`)"
        )
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
            .addMigrations(MIGRATION_1_2)
            .addCallback(DatabaseCallback())
            .build()

    @Provides
    @Singleton
    fun provideTransactionRunner(database: AppDatabase): TransactionRunner =
        RoomTransactionRunner(database)

    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideDebtDao(db: AppDatabase): DebtDao = db.debtDao()
    @Provides fun provideDebtScheduleDao(db: AppDatabase): DebtScheduleDao = db.debtScheduleDao()
    @Provides fun provideDailyBudgetDao(db: AppDatabase): DailyBudgetDao = db.dailyBudgetDao()
    @Provides fun provideMonthlyExpenseDao(db: AppDatabase): MonthlyExpenseDao = db.monthlyExpenseDao()
    @Provides fun provideDailyExpenseDao(db: AppDatabase): DailyExpenseDao = db.dailyExpenseDao()
    @Provides fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()
}
