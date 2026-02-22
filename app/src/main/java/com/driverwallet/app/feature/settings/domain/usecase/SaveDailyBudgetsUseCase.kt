package com.driverwallet.app.feature.settings.domain.usecase

import com.driverwallet.app.feature.settings.domain.SettingsRepository
import com.driverwallet.app.feature.settings.domain.model.DailyBudget
import javax.inject.Inject

class SaveDailyBudgetsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(budgets: Map<String, Long>): Result<Unit> {
        return runCatching {
            val existing = settingsRepository.getDailyBudgets()
            val existingMap = existing.associateBy { it.category }

            val domainBudgets = budgets.map { (category, amount) ->
                require(amount >= 0) { "Budget untuk $category tidak boleh negatif" }
                DailyBudget(
                    id = existingMap[category]?.id ?: 0,
                    category = category,
                    amount = amount,
                )
            }
            settingsRepository.saveBudgets(domainBudgets)
        }
    }
}
