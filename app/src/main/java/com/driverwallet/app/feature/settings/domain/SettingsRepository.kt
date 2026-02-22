package com.driverwallet.app.feature.settings.domain

import com.driverwallet.app.feature.settings.domain.model.DailyBudget
import com.driverwallet.app.feature.settings.domain.model.DailyExpense
import com.driverwallet.app.feature.settings.domain.model.MonthlyExpense
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    // Daily Budgets
    fun observeDailyBudgets(): Flow<List<DailyBudget>>
    suspend fun getDailyBudgets(): List<DailyBudget>
    suspend fun saveBudgets(budgets: List<DailyBudget>)
    suspend fun getTotalDailyBudget(): Long

    // Monthly Expenses
    fun observeMonthlyExpenses(): Flow<List<MonthlyExpense>>
    suspend fun saveMonthlyExpense(expense: MonthlyExpense)
    suspend fun deleteMonthlyExpense(id: Long)
    suspend fun getTotalMonthlyExpense(): Long

    // Daily Expenses
    fun observeDailyExpenses(): Flow<List<DailyExpense>>
    suspend fun saveDailyExpense(expense: DailyExpense)
    suspend fun deleteDailyExpense(id: Long)
    suspend fun getTotalDailyExpense(): Long

    // Settings key-value
    suspend fun getSetting(key: String): String?
    fun observeSetting(key: String): Flow<String?>
    suspend fun saveSetting(key: String, value: String)
}
