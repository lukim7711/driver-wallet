package com.driverwallet.app.feature.debt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.driverwallet.app.feature.debt.data.entity.DebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debt: DebtEntity)

    @Query("SELECT * FROM debts WHERE is_deleted = 0 AND status = 'active' ORDER BY created_at DESC")
    fun observeActiveDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id AND is_deleted = 0")
    suspend fun getById(id: String): DebtEntity?

    @Query("SELECT COALESCE(SUM(remaining_amount), 0) FROM debts WHERE is_deleted = 0 AND status = 'active'")
    fun observeTotalRemaining(): Flow<Long>

    @Query("SELECT COALESCE(SUM(remaining_amount), 0) FROM debts WHERE is_deleted = 0 AND status = 'active'")
    suspend fun getTotalRemaining(): Long

    @Query("UPDATE debts SET remaining_amount = :remaining, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateRemaining(id: String, remaining: Long, updatedAt: String)

    @Query("UPDATE debts SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: String)

    @Query("UPDATE debts SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: String)
}
