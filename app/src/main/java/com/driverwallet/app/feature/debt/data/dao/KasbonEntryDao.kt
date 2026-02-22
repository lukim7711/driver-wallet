package com.driverwallet.app.feature.debt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.driverwallet.app.feature.debt.data.entity.KasbonEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KasbonEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: KasbonEntryEntity)

    @Query("SELECT * FROM kasbon_entries WHERE debt_id = :debtId ORDER BY created_at DESC")
    fun observeByDebtId(debtId: String): Flow<List<KasbonEntryEntity>>

    @Query("SELECT * FROM kasbon_entries WHERE debt_id = :debtId ORDER BY created_at DESC")
    suspend fun getByDebtId(debtId: String): List<KasbonEntryEntity>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM kasbon_entries WHERE debt_id = :debtId")
    suspend fun getTotalByDebtId(debtId: String): Long

    @Query("SELECT COUNT(*) FROM kasbon_entries WHERE debt_id = :debtId")
    suspend fun countByDebtId(debtId: String): Int
}
