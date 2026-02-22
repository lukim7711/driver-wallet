package com.driverwallet.app.core.database

import android.content.Context
import androidx.room.Room
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
            .addCallback(DatabaseCallback())
            .build()

    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideDebtDao(db: AppDatabase): DebtDao = db.debtDao()
    @Provides fun provideDebtScheduleDao(db: AppDatabase): DebtScheduleDao = db.debtScheduleDao()
    @Provides fun provideDailyBudgetDao(db: AppDatabase): DailyBudgetDao = db.dailyBudgetDao()
    @Provides fun provideMonthlyExpenseDao(db: AppDatabase): MonthlyExpenseDao = db.monthlyExpenseDao()
    @Provides fun provideDailyExpenseDao(db: AppDatabase): DailyExpenseDao = db.dailyExpenseDao()
    @Provides fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()
}
