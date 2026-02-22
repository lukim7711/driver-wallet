package com.driverwallet.app.feature.report.domain.usecase

import com.driverwallet.app.feature.report.domain.model.WeeklyReport
import com.driverwallet.app.shared.domain.model.DailySummary
import com.driverwallet.app.shared.domain.repository.TransactionRepository
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

        // SQL aggregation — returns ~14 rows max (7 days × 2 types)
        val dailySummaries = transactionRepository.getDailySummary(startStr, endStr)

        // Fill gaps for days with no transactions
        val summaryMap = dailySummaries.associateBy { it.date }
        val allDays = (0L..6L).map { dayOffset ->
            val dateStr = monday.plusDays(dayOffset).toString()
            summaryMap[dateStr] ?: DailySummary(date = dateStr)
        }

        return WeeklyReport(
            startDate = monday.toString(),
            endDate = sunday.toString(),
            dailySummaries = allDays,
            totalIncome = allDays.sumOf { it.income },
            totalExpense = allDays.sumOf { it.expense },
        )
    }
}
