package com.driverwallet.app.feature.debt.domain.usecase

import com.driverwallet.app.core.model.nowJakarta
import com.driverwallet.app.core.util.UuidGenerator
import com.driverwallet.app.feature.debt.data.entity.DebtEntity
import com.driverwallet.app.feature.debt.data.entity.DebtScheduleEntity
import com.driverwallet.app.feature.debt.domain.DebtRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SaveDebtUseCase @Inject constructor(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(params: DebtFormParams): Result<Unit> {
        // Validation
        if (params.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Nama hutang harus diisi"))
        }
        if (params.totalAmount <= 0) {
            return Result.failure(IllegalArgumentException("Total hutang harus lebih dari 0"))
        }
        if (params.installmentCount <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah cicilan harus lebih dari 0"))
        }
        if (params.installmentPerMonth <= 0) {
            return Result.failure(IllegalArgumentException("Cicilan per bulan harus lebih dari 0"))
        }
        if (params.dueDay !in 1..31) {
            return Result.failure(IllegalArgumentException("Tanggal jatuh tempo harus 1-31"))
        }

        val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        val debtId = UuidGenerator.generate()

        val debt = DebtEntity(
            id = debtId,
            name = params.name,
            platform = params.platform,
            totalAmount = params.totalAmount,
            remainingAmount = params.totalAmount,
            installmentPerMonth = params.installmentPerMonth,
            installmentCount = params.installmentCount,
            interestRate = params.interestRate,
            penaltyType = params.penaltyType,
            penaltyRate = params.penaltyRate,
            dueDay = params.dueDay,
            note = params.note,
            status = "active",
            startDate = params.startDate.toString(),
            createdAt = now,
            updatedAt = now,
        )

        val schedules = generateSchedules(
            debtId = debtId,
            installmentCount = params.installmentCount,
            installmentPerMonth = params.installmentPerMonth,
            dueDay = params.dueDay,
            startDate = params.startDate,
            now = now,
        )

        return runCatching { repository.saveDebt(debt, schedules) }
    }

    private fun generateSchedules(
        debtId: String,
        installmentCount: Int,
        installmentPerMonth: Long,
        dueDay: Int,
        startDate: LocalDate,
        now: String,
    ): List<DebtScheduleEntity> {
        return (1..installmentCount).map { index ->
            val scheduleMonth = startDate.plusMonths(index.toLong() - 1)
            val safeDueDay = minOf(dueDay, scheduleMonth.lengthOfMonth())
            val dueDate = scheduleMonth.withDayOfMonth(safeDueDay)

            DebtScheduleEntity(
                id = UuidGenerator.generate(),
                debtId = debtId,
                installmentNumber = index,
                dueDate = dueDate.toString(),
                expectedAmount = installmentPerMonth,
                actualAmount = null,
                status = "upcoming",
                paidAt = null,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

data class DebtFormParams(
    val name: String,
    val platform: String,
    val totalAmount: Long,
    val installmentPerMonth: Long,
    val installmentCount: Int,
    val dueDay: Int,
    val interestRate: Double = 0.0,
    val penaltyType: String = "none",
    val penaltyRate: Double = 0.0,
    val note: String = "",
    val startDate: LocalDate = LocalDate.now(),
)
