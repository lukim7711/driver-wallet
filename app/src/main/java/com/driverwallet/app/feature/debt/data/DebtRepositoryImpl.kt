package com.driverwallet.app.feature.debt.data

import androidx.room.withTransaction
import com.driverwallet.app.core.database.AppDatabase
import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.core.model.nowJakarta
import com.driverwallet.app.core.util.UuidGenerator
import com.driverwallet.app.feature.debt.data.dao.DebtDao
import com.driverwallet.app.feature.debt.data.dao.DebtScheduleDao
import com.driverwallet.app.feature.debt.data.mapper.toDomain
import com.driverwallet.app.feature.debt.data.mapper.toEntity
import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.debt.domain.DebtWithScheduleInfo
import com.driverwallet.app.feature.debt.domain.model.Debt
import com.driverwallet.app.feature.debt.domain.model.DebtSchedule
import com.driverwallet.app.feature.debt.domain.model.UpcomingDue
import com.driverwallet.app.shared.data.dao.TransactionDao
import com.driverwallet.app.shared.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DebtRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val debtDao: DebtDao,
    private val debtScheduleDao: DebtScheduleDao,
    private val transactionDao: TransactionDao,
) : DebtRepository {

    override fun observeActiveDebtsWithSchedule(): Flow<List<DebtWithScheduleInfo>> =
        debtDao.observeActiveDebts().map { debts ->
            debts.map { debtEntity ->
                DebtWithScheduleInfo(
                    debt = debtEntity.toDomain(),
                    nextSchedule = debtScheduleDao.getNextUnpaid(debtEntity.id)?.toDomain(),
                    paidCount = debtScheduleDao.countPaid(debtEntity.id),
                    totalCount = debtEntity.installmentCount,
                )
            }
        }

    override fun observeTotalRemaining(): Flow<Long> =
        debtDao.observeTotalRemaining()

    override suspend fun getById(id: String): Debt? =
        debtDao.getById(id)?.toDomain()

    override suspend fun saveDebt(debt: Debt, schedules: List<DebtSchedule>) {
        database.withTransaction {
            debtDao.insert(debt.toEntity())
            debtScheduleDao.insertAll(schedules.map { it.toEntity() })
        }
    }

    /**
     * Atomic 4-step payment:
     * 1. Update remaining amount on debt
     * 2. Mark schedule as paid
     * 3. Insert expense transaction (category = debt_payment)
     * 4. Auto-complete debt if remaining = 0
     */
    override suspend fun payInstallment(
        debtId: String,
        scheduleId: String,
        amount: Long,
    ) {
        database.withTransaction {
            val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)

            // 1. Update remaining
            val debt = debtDao.getById(debtId) ?: return@withTransaction
            val newRemaining = (debt.remainingAmount - amount).coerceAtLeast(0)
            debtDao.updateRemaining(debtId, newRemaining, now)

            // 2. Mark schedule paid
            debtScheduleDao.markAsPaid(scheduleId, now, amount, now)

            // 3. Insert expense transaction
            transactionDao.insert(
                TransactionEntity(
                    id = UuidGenerator.generate(),
                    type = TransactionType.EXPENSE.name,
                    category = "debt_payment",
                    amount = amount,
                    note = "Bayar cicilan ${debt.name}",
                    debtId = debtId,
                    createdAt = now,
                    updatedAt = now,
                ),
            )

            // 4. Auto-complete if fully paid
            if (newRemaining == 0L) {
                debtDao.updateStatus(debtId, "completed", now)
            }
        }
    }

    override suspend fun softDelete(debtId: String) {
        val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        debtDao.softDelete(debtId, now)
    }

    override suspend fun getUpcomingDue(maxDate: String): List<UpcomingDue> =
        debtScheduleDao.getUpcomingDue(maxDate).map { it.toDomain() }

    override suspend fun markOverdueSchedules(today: String) {
        val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        debtScheduleDao.markOverdueSchedules(today, now)
    }
}
