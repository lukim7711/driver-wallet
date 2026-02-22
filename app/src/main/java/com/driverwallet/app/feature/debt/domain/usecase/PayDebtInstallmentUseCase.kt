package com.driverwallet.app.feature.debt.domain.usecase

import com.driverwallet.app.feature.debt.domain.DebtRepository
import javax.inject.Inject

class PayDebtInstallmentUseCase @Inject constructor(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(
        debtId: String,
        scheduleId: String,
        amount: Long,
    ): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah bayar harus lebih dari 0"))
        }
        return runCatching {
            repository.payInstallment(debtId, scheduleId, amount)
        }
    }
}
