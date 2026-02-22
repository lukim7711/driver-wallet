package com.driverwallet.app.feature.report.domain.usecase

import com.driverwallet.app.feature.report.domain.model.MonthlyReport
import com.driverwallet.app.shared.domain.repository.TransactionRepository
import java.time.YearMonth
import javax.inject.Inject

class GetMonthlyReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(year: Int, month: Int): MonthlyReport {
        val yearMonth = YearMonth.of(year, month)
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()

        val startStr = "${firstDay}T00:00:00"
        val endStr = "${lastDay}T23:59:59"

        // Two lightweight SQL queries instead of loading all transactions
        val dailySummaries = transactionRepository.getDailySummary(startStr, endStr)
        val totalIncome = dailySummaries.sumOf { it.income }
        val totalExpense = dailySummaries.sumOf { it.expense }

        val categoryBreakdown = transactionRepository.getCategorySummary(startStr, endStr)

        return MonthlyReport(
            month = yearMonth.toString(),
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            categoryBreakdown = categoryBreakdown,
        )
    }
}
