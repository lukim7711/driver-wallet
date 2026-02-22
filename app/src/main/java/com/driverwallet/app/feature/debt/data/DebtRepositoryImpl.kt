package com.driverwallet.app.feature.debt.data

import com.driverwallet.app.core.database.TransactionRunner
import com.driverwallet.app.core.model.TransactionSource
import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.core.model.nowJakarta
import com.driverwallet.app.core.util.UuidGenerator
import com.driverwallet.app.feature.debt.data.dao.DebtDao
import com.driverwallet.app.feature.debt.data.dao.DebtPaymentDao
import com.driverwallet.app.feature.debt.data.dao.DebtScheduleDao
import com.driverwallet.app.feature.debt.data.dao.KasbonEntryDao
import com.driverwallet.app.feature.debt.data.entity.DebtPaymentEntity
import com.driverwallet.app.feature.debt.data.entity.KasbonEntryEntity
import com.driverwallet.app.feature.debt.data.mapper.toDomain
import com.driverwallet.app.feature.debt.data.mapper.toEntity
import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.debt.domain.DebtWithScheduleInfo
import com.driverwallet.app.feature.debt.domain.model.Debt
import com.driverwallet.app.feature.debt.domain.model.DebtPayment
import com.driverwallet.app.feature.debt.domain.model.DebtSchedule
import com.driverwallet.app.feature.debt.domain.model.DebtStatus
import com.driverwallet.app.feature.debt.domain.model.DebtType
import com.driverwallet.app.feature.debt.domain.model.KasbonEntry
import com.driverwallet.app.feature.debt.domain.model.UpcomingDue
import com.driverwallet.app.shared.data.dao.TransactionDao
import com.driverwallet.app.shared.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DebtRepositoryImpl @Inject constructor(
    private val transactionRunner: TransactionRunner,
    private val debtDao: DebtDao,
    private val debtScheduleDao: DebtScheduleDao,
    private val debtPaymentDao: DebtPaymentDao,
    private val kasbonEntryDao: KasbonEntryDao,
    private val transactionDao: TransactionDao,
) : DebtRepository {

    // ==================== Observe ====================

    override fun observeActiveDebtsWithSchedule(): Flow<List<DebtWithScheduleInfo>> =
        debtDao.observeActiveDebts().map { debts ->
            debts.map { debtEntity ->
                val type = DebtType.fromValue(debtEntity.debtType)
                when (type) {
                    DebtType.INSTALLMENT -> DebtWithScheduleInfo(
                        debt = debtEntity.toDomain(),
                        nextSchedule = debtScheduleDao.getNextUnpaid(debtEntity.id)?.toDomain(),
                        paidCount = debtScheduleDao.countPaid(debtEntity.id),
                        totalCount = debtEntity.installmentCount,
                    )
                    DebtType.PERSONAL, DebtType.TAB -> DebtWithScheduleInfo(
                        debt = debtEntity.toDomain(),
                        nextSchedule = null,
                        paidCount = debtPaymentDao.countByDebtId(debtEntity.id),
                        totalCount = 0, // no fixed schedule
                    )
                }
            }
        }

    override fun observeTotalRemaining(): Flow<Long> =
        debtDao.observeTotalRemaining()

    override fun observeKasbonEntries(debtId: String): Flow<List<KasbonEntry>> =
        kasbonEntryDao.observeByDebtId(debtId).map { list -> list.map { it.toDomain() } }

    override fun observePayments(debtId: String): Flow<List<DebtPayment>> =
        debtPaymentDao.observeByDebtId(debtId).map { list -> list.map { it.toDomain() } }

    // ==================== Read ====================

    override suspend fun getById(id: String): Debt? =
        debtDao.getById(id)?.toDomain()

    // ==================== Write ====================

    override suspend fun saveDebt(debt: Debt, schedules: List<DebtSchedule>) {
        transactionRunner.withTransaction {
            debtDao.insert(debt.toEntity())
            if (schedules.isNotEmpty()) {
                debtScheduleDao.insertAll(schedules.map { it.toEntity() })
            }
        }
    }

    /**
     * INSTALLMENT payment: schedule-based, 4-step atomic.
     * 1. Update remaining amount
     * 2. Mark schedule as paid
     * 3. Insert expense transaction (source = DEBT_PAYMENT)
     * 4. Auto-complete if remaining = 0
     */
    override suspend fun payInstallment(
        debtId: String,
        scheduleId: String,
        amount: Long,
    ) {
        transactionRunner.withTransaction {
            val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
            val debt = debtDao.getById(debtId) ?: return@withTransaction
            val newRemaining = (debt.remainingAmount - amount).coerceAtLeast(0)

            debtDao.updateRemaining(debtId, newRemaining, now)
            debtScheduleDao.markAsPaid(scheduleId, now, amount, now)
            insertDebtPaymentTransaction(debt.name, debtId, amount, now)

            if (newRemaining == 0L) {
                debtDao.updateStatus(debtId, DebtStatus.COMPLETED.value, now)
            }
        }
    }

    /**
     * PERSONAL / TAB payment: no schedule, flexible amount.
     * 1. Insert DebtPayment record
     * 2. Update remaining amount
     * 3. Insert expense transaction (source = DEBT_PAYMENT)
     * 4. Auto-complete if remaining = 0
     */
    override suspend fun payDebt(debtId: String, amount: Long, note: String) {
        transactionRunner.withTransaction {
            val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
            val debt = debtDao.getById(debtId) ?: return@withTransaction
            val newRemaining = (debt.remainingAmount - amount).coerceAtLeast(0)

            // 1. Record payment
            debtPaymentDao.insert(
                DebtPaymentEntity(
                    id = UuidGenerator.generate(),
                    debtId = debtId,
                    amount = amount,
                    note = note,
                    paidAt = now,
                    createdAt = now,
                ),
            )

            // 2. Update remaining
            debtDao.updateRemaining(debtId, newRemaining, now)

            // 3. Insert expense transaction
            insertDebtPaymentTransaction(debt.name, debtId, amount, now)

            // 4. Auto-complete if fully paid
            if (newRemaining == 0L) {
                debtDao.updateStatus(debtId, DebtStatus.COMPLETED.value, now)
            }
        }
    }

    /**
     * TAB only: add kasbon entry. This INCREASES both totalAmount and remainingAmount.
     * No expense transaction created — kasbon is "adding debt", not spending.
     */
    override suspend fun addKasbonEntry(debtId: String, amount: Long, note: String) {
        transactionRunner.withTransaction {
            val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
            val debt = debtDao.getById(debtId) ?: return@withTransaction

            // Insert kasbon entry
            kasbonEntryDao.insert(
                KasbonEntryEntity(
                    id = UuidGenerator.generate(),
                    debtId = debtId,
                    amount = amount,
                    note = note,
                    createdAt = now,
                ),
            )

            // Increase total and remaining
            debtDao.updateAmounts(
                debtId = debtId,
                totalAmount = debt.totalAmount + amount,
                remainingAmount = debt.remainingAmount + amount,
                updatedAt = now,
            )
        }
    }

    override suspend fun softDelete(debtId: String) {
        val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        debtDao.softDelete(debtId, now)
    }

    // ==================== Schedule management ====================

    override suspend fun getUpcomingDue(maxDate: String): List<UpcomingDue> =
        debtScheduleDao.getUpcomingDue(maxDate).map { it.toDomain() }

    override suspend fun markOverdueSchedules(today: String) {
        val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        debtScheduleDao.markOverdueSchedules(today, now)
    }

    // ==================== Private helpers ====================

    private suspend fun insertDebtPaymentTransaction(
        debtName: String,
        debtId: String,
        amount: Long,
        now: String,
    ) {
        transactionDao.insert(
            TransactionEntity(
                id = UuidGenerator.generate(),
                type = TransactionType.EXPENSE.name,
                category = "debt_payment",
                amount = amount,
                note = "Bayar hutang $debtName",
                source = TransactionSource.DEBT_PAYMENT.value,
                debtId = debtId,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }
}
