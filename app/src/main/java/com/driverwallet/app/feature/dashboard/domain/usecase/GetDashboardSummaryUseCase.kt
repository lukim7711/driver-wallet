package com.driverwallet.app.feature.dashboard.domain.usecase

import com.driverwallet.app.core.model.UrgencyLevel
import com.driverwallet.app.core.model.todayJakarta
import com.driverwallet.app.feature.dashboard.domain.model.BudgetInfo
import com.driverwallet.app.feature.dashboard.domain.model.DashboardData
import com.driverwallet.app.feature.dashboard.domain.model.DueAlert
import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.settings.domain.SettingsRepository
import com.driverwallet.app.shared.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetDashboardSummaryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val debtRepository: DebtRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateDailyTarget: CalculateDailyTargetUseCase,
) {
    suspend operator fun invoke(): DashboardData {
        val today = todayJakarta()
        val todayPrefix = today.toString()
        val yesterdayPrefix = today.minusDays(1).toString()

        // 1. Mark overdue schedules first
        debtRepository.markOverdueSchedules(todayPrefix)

        // 2. Today's summary
        val todaySummary = transactionRepository.getTodaySummary(todayPrefix)

        // 3. Yesterday's summary for % change
        val yesterdaySummary = transactionRepository.getTodaySummary(yesterdayPrefix)
        val yesterdayProfit = yesterdaySummary.profit

        // 4. Budget info
        val totalBudget = settingsRepository.getTotalDailyBudget()
        val budgetCategories = listOf("fuel", "food", "cigarette", "phone")
        val spentToday = transactionRepository.getBudgetSpentToday(todayPrefix, budgetCategories)
        val budgetInfo = BudgetInfo(totalBudget = totalBudget, spentToday = spentToday)

        // 5. Due alerts (next 7 days)
        val maxDate = today.plusDays(7).toString()
        val upcomingDue = debtRepository.getUpcomingDue(maxDate)
        val dueAlerts = upcomingDue.map { tuple ->
            val dueDate = LocalDate.parse(tuple.dueDate)
            val daysUntil = ChronoUnit.DAYS.between(today, dueDate)
            DueAlert(
                debtId = tuple.debtId,
                debtName = tuple.debtName,
                platform = tuple.platform,
                dueDate = tuple.dueDate,
                amount = tuple.expectedAmount,
                installmentNumber = tuple.installmentNumber,
                urgency = UrgencyLevel.fromDaysUntilDue(daysUntil),
            )
        }

        // 6. Recent transactions (today, max 5)
        val recentTransactions = transactionRepository
            .observeTodayTransactions()
            .first()
            .take(5)

        // 7. Daily target
        val dailyTarget = calculateDailyTarget(earnedToday = todaySummary.profit)

        return DashboardData(
            todaySummary = todaySummary,
            dailyTarget = dailyTarget,
            budgetInfo = budgetInfo,
            dueAlerts = dueAlerts,
            recentTransactions = recentTransactions,
            yesterdayProfit = yesterdayProfit,
        )
    }
}
