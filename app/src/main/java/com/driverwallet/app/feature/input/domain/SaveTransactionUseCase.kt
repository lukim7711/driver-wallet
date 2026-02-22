package com.driverwallet.app.feature.input.domain

import com.driverwallet.app.shared.data.repository.TransactionRepository
import com.driverwallet.app.shared.domain.model.Transaction
import javax.inject.Inject

class SaveTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        if (transaction.amount <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah harus lebih dari 0"))
        }
        if (transaction.amount > MAX_AMOUNT) {
            return Result.failure(IllegalArgumentException("Jumlah maksimal Rp 999.999.999"))
        }
        if (transaction.note.length > MAX_NOTE_LENGTH) {
            return Result.failure(IllegalArgumentException("Catatan maksimal 100 karakter"))
        }
        if (transaction.category == null) {
            return Result.failure(IllegalArgumentException("Pilih kategori"))
        }
        return runCatching { repository.insert(transaction) }
    }

    companion object {
        const val MAX_AMOUNT = 999_999_999L
        const val MAX_NOTE_LENGTH = 100
        const val MAX_DIGITS = 9
    }
}
