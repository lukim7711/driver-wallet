package com.driverwallet.app.feature.dashboard.domain.usecase

import com.driverwallet.app.core.model.todayJakarta
import com.driverwallet.app.feature.dashboard.domain.model.DailyTarget
import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.settings.domain.SettingsKeys
import com.driverwallet.app.feature.settings.domain.SettingsRepository
import com.driverwallet.app.feature.settings.domain.model.RecurringFrequency
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateDailyTargetUseCase @Inject constructor(
    private val debtRepository: DebtRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(earnedToday: Long): DailyTarget {
        val today = todayJakarta()

        // Check rest day
        val restDaysStr = settingsRepository.getSetting(SettingsKeys.REST_DAYS) ?: "0"
        val restDays = restDaysStr.split(",").mapNotNull { it.trim().toIntOrNull() }
        val todayDow = today.dayOfWeek.value % 7
        val isRestDay = todayDow in restDays

        if (isRestDay) {
            return DailyTarget(targetAmount = 0L, earnedAmount = earnedToday, isRestDay = true)
        }

        // Daily debt target
        val totalDebtRemaining = debtRepository.observeTotalRemaining().first()
        val targetDateStr = settingsRepository.getSetting(SettingsKeys.DEBT_TARGET_DATE)
        val dailyDebtTarget = if (!targetDateStr.isNullOrEmpty()) {
            runCatching {
                val targetDate = LocalDate.parse(targetDateStr)
                val daysRemaining = ChronoUnit.DAYS.between(today, targetDate).coerceAtLeast(1)
                totalDebtRemaining / daysRemaining
            }.getOrDefault(0L)
        } else {
            0L
        }

        // Monthly expenses prorated to daily
        val totalMonthly = settingsRepository.getTotalRecurringExpense(RecurringFrequency.MONTHLY)
        val daysInMonth = today.lengthOfMonth().toLong().coerceAtLeast(1)
        val dailyProrated = totalMonthly / daysInMonth

        // Daily fixed expenses (includes former budget items)
        val dailyFixed = settingsRepository.getTotalRecurringExpense(RecurringFrequency.DAILY)

        val targetAmount = dailyDebtTarget + dailyProrated + dailyFixed

        return DailyTarget(
            targetAmount = targetAmount,
            earnedAmount = earnedToday,
            isRestDay = false,
        )
    }
}
