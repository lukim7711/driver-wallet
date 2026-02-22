package com.driverwallet.app.feature.report.domain.usecase

import com.driverwallet.app.feature.report.domain.model.DailySummary
import com.driverwallet.app.feature.report.domain.model.WeeklyReport
import com.driverwallet.app.feature.transaction.domain.TransactionRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class GetWeeklyReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(weekStartDate: LocalDate): WeeklyReport {
        val monday = weekStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(6)

        val startStr = "${monday}T00:00:00"
        val endStr = "${sunday}T23:59:59"

        val transactions = transactionRepository.getByDateRange(startStr, endStr)

        // Group by date, ensure all 7 days present
        val grouped = transactions.groupBy {
            LocalDate.parse(it.dateTime.substring(0, 10))
        }

        val dailySummaries = (0L..6L).map { dayOffset ->
            val date = monday.plusDays(dayOffset)
            val dayTransactions = grouped[date].orEmpty()
            DailySummary(
                date = date,
                income = dayTransactions.filter { it.type == "income" }.sumOf { it.amount },
                expense = dayTransactions.filter { it.type == "expense" }.sumOf { it.amount },
                transactionCount = dayTransactions.size,
            )
        }

        return WeeklyReport(
            startDate = monday,
            endDate = sunday,
            dailySummaries = dailySummaries,
            totalIncome = dailySummaries.sumOf { it.income },
            totalExpense = dailySummaries.sumOf { it.expense },
        )
    }
}
