package com.driverwallet.app.feature.settings.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.driverwallet.app.feature.settings.data.entity.MonthlyExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyExpenseDao {

    @Query("SELECT * FROM monthly_expenses WHERE is_deleted = 0 ORDER BY id ASC")
    fun observeAll(): Flow<List<MonthlyExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(expense: MonthlyExpenseEntity)

    @Query("UPDATE monthly_expenses SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM monthly_expenses WHERE is_deleted = 0")
    suspend fun getTotalAmount(): Long
}
