package com.driverwallet.app.feature.settings.data

import com.driverwallet.app.feature.settings.data.dao.DailyBudgetDao
import com.driverwallet.app.feature.settings.data.dao.DailyExpenseDao
import com.driverwallet.app.feature.settings.data.dao.MonthlyExpenseDao
import com.driverwallet.app.feature.settings.data.dao.SettingsDao
import com.driverwallet.app.feature.settings.data.entity.DailyBudgetEntity
import com.driverwallet.app.feature.settings.data.entity.DailyExpenseEntity
import com.driverwallet.app.feature.settings.data.entity.MonthlyExpenseEntity
import com.driverwallet.app.feature.settings.data.entity.SettingsEntity
import com.driverwallet.app.feature.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dailyBudgetDao: DailyBudgetDao,
    private val monthlyExpenseDao: MonthlyExpenseDao,
    private val dailyExpenseDao: DailyExpenseDao,
    private val settingsDao: SettingsDao,
) : SettingsRepository {

    // Daily Budgets
    override fun observeDailyBudgets(): Flow<List<DailyBudgetEntity>> =
        dailyBudgetDao.observeAll()

    override suspend fun getDailyBudgets(): List<DailyBudgetEntity> =
        dailyBudgetDao.getAll()

    override suspend fun saveBudgets(budgets: List<DailyBudgetEntity>) =
        dailyBudgetDao.upsertAll(budgets)

    override suspend fun getTotalDailyBudget(): Long =
        dailyBudgetDao.getTotalBudget()

    // Monthly Expenses
    override fun observeMonthlyExpenses(): Flow<List<MonthlyExpenseEntity>> =
        monthlyExpenseDao.observeAll()

    override suspend fun saveMonthlyExpense(expense: MonthlyExpenseEntity) =
        monthlyExpenseDao.upsert(expense)

    override suspend fun deleteMonthlyExpense(id: Long) =
        monthlyExpenseDao.softDelete(id)

    override suspend fun getTotalMonthlyExpense(): Long =
        monthlyExpenseDao.getTotalAmount()

    // Daily Expenses
    override fun observeDailyExpenses(): Flow<List<DailyExpenseEntity>> =
        dailyExpenseDao.observeAll()

    override suspend fun saveDailyExpense(expense: DailyExpenseEntity) =
        dailyExpenseDao.upsert(expense)

    override suspend fun deleteDailyExpense(id: Long) =
        dailyExpenseDao.softDelete(id)

    override suspend fun getTotalDailyExpense(): Long =
        dailyExpenseDao.getTotalAmount()

    // Settings key-value
    override suspend fun getSetting(key: String): String? =
        settingsDao.getValue(key)

    override fun observeSetting(key: String): Flow<String?> =
        settingsDao.observeValue(key)

    override suspend fun saveSetting(key: String, value: String) =
        settingsDao.upsert(SettingsEntity(key, value))
}
