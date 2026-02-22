package com.driverwallet.app.feature.debt.domain.usecase

import com.driverwallet.app.feature.debt.domain.DebtRepository
import javax.inject.Inject

/**
 * Universal payment for PERSONAL and TAB debts.
 * No schedule needed — just pay whatever amount, whenever.
 *
 * For INSTALLMENT debts, use [PayDebtInstallmentUseCase] instead
 * (requires scheduleId).
 */
class PayDebtUseCase @Inject constructor(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(
        debtId: String,
        amount: Long,
        note: String = "",
    ): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah bayar harus lebih dari 0"))
        }
        return runCatching {
            repository.payDebt(debtId, amount, note)
        }
    }
}
