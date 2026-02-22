package com.driverwallet.app.feature.settings.domain.usecase

import com.driverwallet.app.feature.settings.domain.SettingsRepository
import com.driverwallet.app.feature.settings.domain.model.RecurringExpense
import com.driverwallet.app.feature.settings.domain.model.RecurringFrequency
import javax.inject.Inject

class SaveRecurringExpenseUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(
        id: Long = 0,
        name: String,
        amount: Long,
        icon: String = "payments",
        frequency: RecurringFrequency,
    ): Result<Unit> {
        return runCatching {
            require(name.isNotBlank()) { "Nama tidak boleh kosong" }
            require(amount > 0) { "Jumlah harus lebih dari 0" }
            settingsRepository.saveRecurringExpense(
                RecurringExpense(
                    id = id,
                    name = name,
                    icon = icon,
                    amount = amount,
                    frequency = frequency,
                ),
            )
        }
    }
}
