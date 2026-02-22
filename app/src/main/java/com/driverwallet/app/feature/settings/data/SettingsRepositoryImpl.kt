package com.driverwallet.app.feature.settings.data

import com.driverwallet.app.feature.settings.data.dao.DailyBudgetDao
import com.driverwallet.app.feature.settings.data.dao.DailyExpenseDao
import com.driverwallet.app.feature.settings.data.dao.MonthlyExpenseDao
import com.driverwallet.app.feature.settings.data.dao.SettingsDao
import com.driverwallet.app.feature.settings.data.entity.SettingsEntity
import com.driverwallet.app.feature.settings.data.mapper.toDomain
import com.driverwallet.app.feature.settings.data.mapper.toEntity
import com.driverwallet.app.feature.settings.domain.SettingsRepository
import com.driverwallet.app.feature.settings.domain.model.DailyBudget
import com.driverwallet.app.feature.settings.domain.model.DailyExpense
import com.driverwallet.app.feature.settings.domain.model.MonthlyExpense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dailyBudgetDao: DailyBudgetDao,
    private val monthlyExpenseDao: MonthlyExpenseDao,
    private val dailyExpenseDao: DailyExpenseDao,
    private val settingsDao: SettingsDao,
) : SettingsRepository {

    // Daily Budgets
    override fun observeDailyBudgets(): Flow<List<DailyBudget>> =
        dailyBudgetDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getDailyBudgets(): List<DailyBudget> =
        dailyBudgetDao.getAll().map { it.toDomain() }

    override suspend fun saveBudgets(budgets: List<DailyBudget>) =
        dailyBudgetDao.upsertAll(budgets.map { it.toEntity() })

    override suspend fun getTotalDailyBudget(): Long =
        dailyBudgetDao.getTotalBudget()

    // Monthly Expenses
    override fun observeMonthlyExpenses(): Flow<List<MonthlyExpense>> =
        monthlyExpenseDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveMonthlyExpense(expense: MonthlyExpense) =
        monthlyExpenseDao.upsert(expense.toEntity())

    override suspend fun deleteMonthlyExpense(id: Long) =
        monthlyExpenseDao.softDelete(id)

    override suspend fun getTotalMonthlyExpense(): Long =
        monthlyExpenseDao.getTotalAmount()

    // Daily Expenses
    override fun observeDailyExpenses(): Flow<List<DailyExpense>> =
        dailyExpenseDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveDailyExpense(expense: DailyExpense) =
        dailyExpenseDao.upsert(expense.toEntity())

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
