package com.driverwallet.app.feature.settings.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.driverwallet.app.feature.settings.data.entity.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {

    @Query("SELECT * FROM recurring_expenses WHERE is_deleted = 0 ORDER BY id ASC")
    fun observeAll(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE is_deleted = 0 AND frequency = :frequency ORDER BY id ASC")
    fun observeByFrequency(frequency: String): Flow<List<RecurringExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(expense: RecurringExpenseEntity)

    @Query("UPDATE recurring_expenses SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM recurring_expenses WHERE is_deleted = 0 AND frequency = :frequency")
    suspend fun getTotalByFrequency(frequency: String): Long
}
