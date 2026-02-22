package com.driverwallet.app.feature.dashboard.domain.usecase

import com.driverwallet.app.core.model.todayJakarta
import com.driverwallet.app.feature.dashboard.domain.model.DailyTarget
import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.settings.domain.SettingsRepository
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
        val restDaysStr = settingsRepository.getSetting("rest_days") ?: "0"
        val restDays = restDaysStr.split(",").mapNotNull { it.trim().toIntOrNull() }
        val todayDow = today.dayOfWeek.value % 7 // 0=Sun, 1=Mon, ..., 6=Sat
        val isRestDay = todayDow in restDays

        if (isRestDay) {
            return DailyTarget(targetAmount = 0L, earnedAmount = earnedToday, isRestDay = true)
        }

        // Daily debt target
        val totalDebtRemaining = debtRepository.observeTotalRemaining().first()
        val targetDateStr = settingsRepository.getSetting("debt_target_date")
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
        val totalMonthly = settingsRepository.getTotalMonthlyExpense()
        val daysInMonth = today.lengthOfMonth().toLong().coerceAtLeast(1)
        val dailyProrated = totalMonthly / daysInMonth

        // Daily fixed expenses
        val dailyFixed = settingsRepository.getTotalDailyExpense()

        // Daily budget
        val dailyBudget = settingsRepository.getTotalDailyBudget()

        val targetAmount = dailyDebtTarget + dailyProrated + dailyFixed + dailyBudget

        return DailyTarget(
            targetAmount = targetAmount,
            earnedAmount = earnedToday,
            isRestDay = false,
        )
    }
}
