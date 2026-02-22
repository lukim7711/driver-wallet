package com.driverwallet.app.feature.settings.domain.usecase

import com.driverwallet.app.feature.settings.data.entity.DailyExpenseEntity
import com.driverwallet.app.feature.settings.domain.SettingsRepository
import javax.inject.Inject

class SaveDailyExpenseUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(
        id: Long = 0,
        name: String,
        amount: Long,
        icon: String = "payments",
    ): Result<Unit> {
        return runCatching {
            require(name.isNotBlank()) { "Nama tidak boleh kosong" }
            require(amount > 0) { "Jumlah harus lebih dari 0" }
            settingsRepository.saveDailyExpense(
                DailyExpenseEntity(id = id, name = name, icon = icon, amount = amount),
            )
        }
    }
}
