package com.driverwallet.app.feature.settings.data

import com.driverwallet.app.feature.settings.data.dao.RecurringExpenseDao
import com.driverwallet.app.feature.settings.data.dao.SettingsDao
import com.driverwallet.app.feature.settings.data.entity.SettingsEntity
import com.driverwallet.app.feature.settings.data.mapper.toDomain
import com.driverwallet.app.feature.settings.data.mapper.toEntity
import com.driverwallet.app.feature.settings.domain.SettingsRepository
import com.driverwallet.app.feature.settings.domain.model.RecurringExpense
import com.driverwallet.app.feature.settings.domain.model.RecurringFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val recurringExpenseDao: RecurringExpenseDao,
    private val settingsDao: SettingsDao,
) : SettingsRepository {

    // Recurring Expenses
    override fun observeRecurringExpenses(
        frequency: RecurringFrequency,
    ): Flow<List<RecurringExpense>> =
        recurringExpenseDao.observeByFrequency(frequency.value)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun saveRecurringExpense(expense: RecurringExpense) =
        recurringExpenseDao.upsert(expense.toEntity())

    override suspend fun deleteRecurringExpense(id: Long) =
        recurringExpenseDao.softDelete(id)

    override suspend fun getTotalRecurringExpense(frequency: RecurringFrequency): Long =
        recurringExpenseDao.getTotalByFrequency(frequency.value)

    // Settings key-value
    override suspend fun getSetting(key: String): String? =
        settingsDao.getValue(key)

    override fun observeSetting(key: String): Flow<String?> =
        settingsDao.observeValue(key)

    override suspend fun saveSetting(key: String, value: String) =
        settingsDao.upsert(SettingsEntity(key, value))
}
