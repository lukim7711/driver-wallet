package com.driverwallet.app.feature.settings.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.driverwallet.app.feature.settings.data.entity.DailyBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyBudgetDao {

    @Query("SELECT * FROM daily_budgets ORDER BY id ASC")
    suspend fun getAll(): List<DailyBudgetEntity>

    @Query("SELECT * FROM daily_budgets ORDER BY id ASC")
    fun observeAll(): Flow<List<DailyBudgetEntity>>

    @Upsert
    suspend fun upsertAll(budgets: List<DailyBudgetEntity>)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM daily_budgets")
    suspend fun getTotalBudget(): Long
}
