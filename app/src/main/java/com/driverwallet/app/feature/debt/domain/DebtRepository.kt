package com.driverwallet.app.feature.debt.domain

import com.driverwallet.app.feature.debt.domain.model.Debt
import com.driverwallet.app.feature.debt.domain.model.DebtPayment
import com.driverwallet.app.feature.debt.domain.model.DebtSchedule
import com.driverwallet.app.feature.debt.domain.model.KasbonEntry
import com.driverwallet.app.feature.debt.domain.model.UpcomingDue
import kotlinx.coroutines.flow.Flow

/**
 * Aggregated debt info combining debt with schedule/payment progress.
 * Works for all 3 types:
 * - INSTALLMENT: paidCount/totalCount from DebtSchedule
 * - PERSONAL: paidCount from DebtPayment count, totalCount = 0 (no schedule)
 * - TAB: paidCount from DebtPayment count, totalCount = 0 (no schedule)
 */
data class DebtWithScheduleInfo(
    val debt: Debt,
    val nextSchedule: DebtSchedule?,
    val paidCount: Int,
    val totalCount: Int,
) {
    val percentage: Float
        get() = if (totalCount > 0) paidCount.toFloat() / totalCount else debt.progressPercent
}

interface DebtRepository {
    // --- Observe ---
    fun observeActiveDebtsWithSchedule(): Flow<List<DebtWithScheduleInfo>>
    fun observeTotalRemaining(): Flow<Long>
    fun observeKasbonEntries(debtId: String): Flow<List<KasbonEntry>>
    fun observePayments(debtId: String): Flow<List<DebtPayment>>

    // --- Read ---
    suspend fun getById(id: String): Debt?

    // --- Write ---
    suspend fun saveDebt(debt: Debt, schedules: List<DebtSchedule>)
    suspend fun payInstallment(debtId: String, scheduleId: String, amount: Long)
    suspend fun payDebt(debtId: String, amount: Long, note: String)
    suspend fun addKasbonEntry(debtId: String, amount: Long, note: String)
    suspend fun softDelete(debtId: String)

    // --- Schedule management ---
    suspend fun getUpcomingDue(maxDate: String): List<UpcomingDue>
    suspend fun markOverdueSchedules(today: String)
}
