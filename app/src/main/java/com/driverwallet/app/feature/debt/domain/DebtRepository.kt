package com.driverwallet.app.feature.debt.domain

import com.driverwallet.app.feature.debt.domain.model.Debt
import com.driverwallet.app.feature.debt.domain.model.DebtSchedule
import com.driverwallet.app.feature.debt.domain.model.UpcomingDue
import kotlinx.coroutines.flow.Flow

/**
 * Aggregated debt info combining debt with its schedule progress.
 * Now uses domain models instead of Room entities.
 */
data class DebtWithScheduleInfo(
    val debt: Debt,
    val nextSchedule: DebtSchedule?,
    val paidCount: Int,
    val totalCount: Int,
) {
    val percentage: Float
        get() = if (totalCount > 0) paidCount.toFloat() / totalCount else 0f
}

interface DebtRepository {
    fun observeActiveDebtsWithSchedule(): Flow<List<DebtWithScheduleInfo>>
    fun observeTotalRemaining(): Flow<Long>
    suspend fun getById(id: String): Debt?
    suspend fun saveDebt(debt: Debt, schedules: List<DebtSchedule>)
    suspend fun payInstallment(debtId: String, scheduleId: String, amount: Long)
    suspend fun softDelete(debtId: String)
    suspend fun getUpcomingDue(maxDate: String): List<UpcomingDue>
    suspend fun markOverdueSchedules(today: String)
}
