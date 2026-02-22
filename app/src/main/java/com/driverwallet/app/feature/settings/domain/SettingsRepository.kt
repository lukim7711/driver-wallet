package com.driverwallet.app.feature.settings.domain

import com.driverwallet.app.feature.settings.data.entity.DailyBudgetEntity
import com.driverwallet.app.feature.settings.data.entity.DailyExpenseEntity
import com.driverwallet.app.feature.settings.data.entity.MonthlyExpenseEntity
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    // Daily Budgets
    fun observeDailyBudgets(): Flow<List<DailyBudgetEntity>>
    suspend fun getDailyBudgets(): List<DailyBudgetEntity>
    suspend fun saveBudgets(budgets: List<DailyBudgetEntity>)
    suspend fun getTotalDailyBudget(): Long

    // Monthly Expenses
    fun observeMonthlyExpenses(): Flow<List<MonthlyExpenseEntity>>
    suspend fun saveMonthlyExpense(expense: MonthlyExpenseEntity)
    suspend fun deleteMonthlyExpense(id: Long)
    suspend fun getTotalMonthlyExpense(): Long

    // Daily Expenses
    fun observeDailyExpenses(): Flow<List<DailyExpenseEntity>>
    suspend fun saveDailyExpense(expense: DailyExpenseEntity)
    suspend fun deleteDailyExpense(id: Long)
    suspend fun getTotalDailyExpense(): Long

    // Settings key-value
    suspend fun getSetting(key: String): String?
    fun observeSetting(key: String): Flow<String?>
    suspend fun saveSetting(key: String, value: String)
}
