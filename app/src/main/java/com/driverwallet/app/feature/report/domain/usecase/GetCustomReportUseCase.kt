package com.driverwallet.app.feature.report.domain.usecase

import com.driverwallet.app.feature.report.domain.model.CustomReport
import com.driverwallet.app.shared.domain.model.DailySummary
import com.driverwallet.app.shared.domain.repository.TransactionRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetCustomReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): CustomReport {
        val startStr = "${startDate}T00:00:00"
        val endStr = "${endDate}T23:59:59"

        // SQL aggregation — only summary tuples, no full Transaction objects
        val dailySummaries = transactionRepository.getDailySummary(startStr, endStr)

        // Fill gaps for days with no transactions
        val summaryMap = dailySummaries.associateBy { it.date }
        val dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1
        val allDays = (0 until dayCount).map { offset ->
            val dateStr = startDate.plusDays(offset).toString()
            summaryMap[dateStr] ?: DailySummary(date = dateStr)
        }

        return CustomReport(
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            totalIncome = allDays.sumOf { it.income },
            totalExpense = allDays.sumOf { it.expense },
            dailySummaries = allDays,
        )
    }
}
