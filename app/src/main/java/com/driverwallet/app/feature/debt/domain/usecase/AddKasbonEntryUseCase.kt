package com.driverwallet.app.feature.debt.domain.usecase

import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.debt.domain.model.DebtType
import javax.inject.Inject

/**
 * Add a kasbon entry to a TAB debt.
 * This INCREASES the debt’s totalAmount and remainingAmount.
 *
 * Guard: Only allowed for TAB debts. Will fail for INSTALLMENT/PERSONAL.
 */
class AddKasbonEntryUseCase @Inject constructor(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(
        debtId: String,
        amount: Long,
        note: String = "",
    ): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah kasbon harus lebih dari 0"))
        }

        // Guard: only TAB debts can add kasbon entries
        val debt = repository.getById(debtId)
            ?: return Result.failure(IllegalArgumentException("Hutang tidak ditemukan"))

        if (debt.debtType != DebtType.TAB) {
            return Result.failure(
                IllegalStateException("Kasbon hanya bisa ditambahkan ke hutang tipe Tab")
            )
        }

        return runCatching {
            repository.addKasbonEntry(debtId, amount, note)
        }
    }
}
