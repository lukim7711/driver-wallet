package com.driverwallet.app.feature.debt.domain

import com.driverwallet.app.feature.debt.data.dao.UpcomingDueTuple
import com.driverwallet.app.feature.debt.data.entity.DebtEntity
import com.driverwallet.app.feature.debt.data.entity.DebtScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Aggregated debt info combining debt with its schedule progress.
 */
data class DebtWithScheduleInfo(
    val debt: DebtEntity,
    val nextSchedule: DebtScheduleEntity?,
    val paidCount: Int,
    val totalCount: Int,
) {
    val percentage: Float
        get() = if (totalCount > 0) paidCount.toFloat() / totalCount else 0f
}

interface DebtRepository {
    fun observeActiveDebtsWithSchedule(): Flow<List<DebtWithScheduleInfo>>
    fun observeTotalRemaining(): Flow<Long>
    suspend fun getById(id: String): DebtEntity?
    suspend fun saveDebt(debt: DebtEntity, schedules: List<DebtScheduleEntity>)
    suspend fun payInstallment(debtId: String, scheduleId: String, amount: Long)
    suspend fun softDelete(debtId: String)
    suspend fun getUpcomingDue(maxDate: String): List<UpcomingDueTuple>
    suspend fun markOverdueSchedules(today: String)
}
