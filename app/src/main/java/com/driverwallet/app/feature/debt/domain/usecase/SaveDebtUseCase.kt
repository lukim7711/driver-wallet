package com.driverwallet.app.feature.debt.domain.usecase

import com.driverwallet.app.core.model.nowJakarta
import com.driverwallet.app.core.util.UuidGenerator
import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.debt.domain.model.Debt
import com.driverwallet.app.feature.debt.domain.model.DebtDetail
import com.driverwallet.app.feature.debt.domain.model.DebtSchedule
import com.driverwallet.app.feature.debt.domain.model.DebtStatus
import com.driverwallet.app.feature.debt.domain.model.DebtType
import com.driverwallet.app.feature.debt.domain.model.PenaltyType
import com.driverwallet.app.feature.debt.domain.model.ScheduleStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SaveDebtUseCase @Inject constructor(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(params: DebtFormParams): Result<Unit> {
        validate(params).onFailure { return Result.failure(it) }

        val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        val debtId = UuidGenerator.generate()

        val (debtType, detail) = buildDetail(params)

        val debt = Debt(
            id = debtId,
            name = params.name,
            debtType = debtType,
            detail = detail,
            totalAmount = params.totalAmount,
            remainingAmount = params.totalAmount,
            note = params.note,
            status = DebtStatus.ACTIVE,
            startDate = params.startDate.toString(),
            createdAt = now,
            updatedAt = now,
        )

        val schedules = when (params) {
            is DebtFormParams.Installment -> generateSchedules(
                debtId = debtId,
                installmentCount = params.installmentCount,
                installmentPerMonth = params.installmentPerMonth,
                dueDay = params.dueDay,
                startDate = params.startDate,
                now = now,
            )
            is DebtFormParams.Personal,
            is DebtFormParams.Tab -> emptyList() // no fixed schedule
        }

        return runCatching { repository.saveDebt(debt, schedules) }
    }

    // ==================== Validation ====================

    private fun validate(params: DebtFormParams): Result<Unit> {
        if (params.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Nama hutang harus diisi"))
        }
        if (params.totalAmount <= 0) {
            return Result.failure(IllegalArgumentException("Total hutang harus lebih dari 0"))
        }
        return when (params) {
            is DebtFormParams.Installment -> validateInstallment(params)
            is DebtFormParams.Personal -> validatePersonal(params)
            is DebtFormParams.Tab -> validateTab(params)
        }
    }

    private fun validateInstallment(params: DebtFormParams.Installment): Result<Unit> {
        if (params.installmentCount <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah cicilan harus lebih dari 0"))
        }
        if (params.installmentPerMonth <= 0) {
            return Result.failure(IllegalArgumentException("Cicilan per bulan harus lebih dari 0"))
        }
        if (params.dueDay !in 1..31) {
            return Result.failure(IllegalArgumentException("Tanggal jatuh tempo harus 1-31"))
        }
        return Result.success(Unit)
    }

    private fun validatePersonal(params: DebtFormParams.Personal): Result<Unit> {
        if (params.borrowerName.isBlank()) {
            return Result.failure(IllegalArgumentException("Nama orang harus diisi"))
        }
        return Result.success(Unit)
    }

    private fun validateTab(params: DebtFormParams.Tab): Result<Unit> {
        if (params.merchantName.isBlank()) {
            return Result.failure(IllegalArgumentException("Nama tempat harus diisi"))
        }
        return Result.success(Unit)
    }

    // ==================== Build DebtDetail ====================

    private fun buildDetail(params: DebtFormParams): Pair<DebtType, DebtDetail> = when (params) {
        is DebtFormParams.Installment -> DebtType.INSTALLMENT to DebtDetail.Installment(
            platform = params.platform,
            installmentPerMonth = params.installmentPerMonth,
            installmentCount = params.installmentCount,
            dueDay = params.dueDay,
            interestRate = params.interestRate,
            penaltyType = params.penaltyType,
            penaltyRate = params.penaltyRate,
        )
        is DebtFormParams.Personal -> DebtType.PERSONAL to DebtDetail.Personal(
            borrowerName = params.borrowerName,
            relationship = params.relationship,
            agreedReturnDate = params.agreedReturnDate?.toString(),
        )
        is DebtFormParams.Tab -> DebtType.TAB to DebtDetail.Tab(
            merchantName = params.merchantName,
            merchantType = params.merchantType,
        )
    }

    // ==================== Schedule generation (INSTALLMENT only) ====================

    private fun generateSchedules(
        debtId: String,
        installmentCount: Int,
        installmentPerMonth: Long,
        dueDay: Int,
        startDate: LocalDate,
        now: String,
    ): List<DebtSchedule> {
        return (1..installmentCount).map { index ->
            val scheduleMonth = startDate.plusMonths(index.toLong() - 1)
            val safeDueDay = minOf(dueDay, scheduleMonth.lengthOfMonth())
            val dueDate = scheduleMonth.withDayOfMonth(safeDueDay)

            DebtSchedule(
                id = UuidGenerator.generate(),
                debtId = debtId,
                installmentNumber = index,
                dueDate = dueDate.toString(),
                expectedAmount = installmentPerMonth,
                actualAmount = null,
                status = ScheduleStatus.UNPAID,
                paidAt = null,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

// ==================== Sealed params per debt type ====================

sealed class DebtFormParams {
    abstract val name: String
    abstract val totalAmount: Long
    abstract val note: String
    abstract val startDate: LocalDate

    /** Cicilan platform tetap (Shopee, Kredivo, dll) */
    data class Installment(
        override val name: String,
        val platform: String,
        override val totalAmount: Long,
        val installmentPerMonth: Long,
        val installmentCount: Int,
        val dueDay: Int,
        val interestRate: Double = 0.0,
        val penaltyType: PenaltyType = PenaltyType.NONE,
        val penaltyRate: Double = 0.0,
        override val note: String = "",
        override val startDate: LocalDate = LocalDate.now(),
    ) : DebtFormParams()

    /** Hutang pribadi ke orang */
    data class Personal(
        override val name: String,
        val borrowerName: String,
        val relationship: String = "",
        val agreedReturnDate: LocalDate? = null,
        override val totalAmount: Long,
        override val note: String = "",
        override val startDate: LocalDate = LocalDate.now(),
    ) : DebtFormParams()

    /** Kasbon / tab di warung, bengkel */
    data class Tab(
        override val name: String,
        val merchantName: String,
        val merchantType: String = "",
        override val totalAmount: Long,
        override val note: String = "",
        override val startDate: LocalDate = LocalDate.now(),
    ) : DebtFormParams()
}
