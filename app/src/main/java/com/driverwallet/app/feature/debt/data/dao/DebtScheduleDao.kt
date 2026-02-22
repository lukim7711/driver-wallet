package com.driverwallet.app.feature.debt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.driverwallet.app.feature.debt.data.entity.DebtScheduleEntity
import kotlinx.coroutines.flow.Flow

data class UpcomingDueTuple(
    val debtId: String,
    val debtName: String,
    val platform: String,
    val dueDate: String,
    val expectedAmount: Long,
    val installmentNumber: Int,
)

@Dao
interface DebtScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<DebtScheduleEntity>)

    @Query("SELECT * FROM debt_schedules WHERE debt_id = :debtId AND status = 'unpaid' ORDER BY installment_number ASC LIMIT 1")
    suspend fun getNextUnpaid(debtId: String): DebtScheduleEntity?

    @Query("SELECT COUNT(*) FROM debt_schedules WHERE debt_id = :debtId AND status = 'paid'")
    suspend fun countPaid(debtId: String): Int

    @Query("SELECT COUNT(*) FROM debt_schedules WHERE debt_id = :debtId AND status != 'paid'")
    suspend fun countUnpaid(debtId: String): Int

    @Query("""
        SELECT ds.debt_id AS debtId, d.name AS debtName, d.platform,
               ds.due_date AS dueDate, ds.expected_amount AS expectedAmount,
               ds.installment_number AS installmentNumber
        FROM debt_schedules ds
        INNER JOIN debts d ON ds.debt_id = d.id
        WHERE ds.status = 'unpaid' AND ds.due_date <= :maxDate AND d.is_deleted = 0
        ORDER BY ds.due_date ASC
    """)
    suspend fun getUpcomingDue(maxDate: String): List<UpcomingDueTuple>

    @Query("UPDATE debt_schedules SET status = 'paid', paid_at = :paidAt, actual_amount = :actualAmount, updated_at = :updatedAt WHERE id = :id")
    suspend fun markAsPaid(id: String, paidAt: String, actualAmount: Long, updatedAt: String)

    @Query("UPDATE debt_schedules SET status = 'overdue', updated_at = :updatedAt WHERE status = 'unpaid' AND due_date < :today")
    suspend fun markOverdueSchedules(today: String, updatedAt: String)

    @Query("SELECT * FROM debt_schedules WHERE debt_id = :debtId ORDER BY installment_number ASC")
    fun observeByDebtId(debtId: String): Flow<List<DebtScheduleEntity>>
}
