package com.driverwallet.app.feature.settings.domain

import com.driverwallet.app.feature.settings.domain.model.RecurringExpense
import com.driverwallet.app.feature.settings.domain.model.RecurringFrequency
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    // Recurring Expenses (unified)
    fun observeRecurringExpenses(frequency: RecurringFrequency): Flow<List<RecurringExpense>>
    suspend fun saveRecurringExpense(expense: RecurringExpense)
    suspend fun deleteRecurringExpense(id: Long)
    suspend fun getTotalRecurringExpense(frequency: RecurringFrequency): Long

    // Settings key-value
    suspend fun getSetting(key: String): String?
    fun observeSetting(key: String): Flow<String?>
    suspend fun saveSetting(key: String, value: String)
}
