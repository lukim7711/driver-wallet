package com.driverwallet.app.feature.debt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.driverwallet.app.feature.debt.data.entity.DebtPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtPaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: DebtPaymentEntity)

    @Query("SELECT * FROM debt_payments WHERE debt_id = :debtId ORDER BY paid_at DESC")
    fun observeByDebtId(debtId: String): Flow<List<DebtPaymentEntity>>

    @Query("SELECT * FROM debt_payments WHERE debt_id = :debtId ORDER BY paid_at DESC")
    suspend fun getByDebtId(debtId: String): List<DebtPaymentEntity>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM debt_payments WHERE debt_id = :debtId")
    suspend fun getTotalPaidByDebtId(debtId: String): Long

    @Query("SELECT COUNT(*) FROM debt_payments WHERE debt_id = :debtId")
    suspend fun countByDebtId(debtId: String): Int
}
