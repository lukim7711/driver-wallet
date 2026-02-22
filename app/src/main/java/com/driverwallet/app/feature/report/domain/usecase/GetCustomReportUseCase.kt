package com.driverwallet.app.feature.report.domain.usecase

import com.driverwallet.app.feature.report.domain.model.CustomReport
import com.driverwallet.app.feature.report.domain.model.DailySummary
import com.driverwallet.app.feature.transaction.domain.TransactionRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetCustomReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): CustomReport {
        val startStr = "${startDate}T00:00:00"
        val endStr = "${endDate}T23:59:59"

        val transactions = transactionRepository.getByDateRange(startStr, endStr)

        val grouped = transactions.groupBy {
            LocalDate.parse(it.dateTime.substring(0, 10))
        }

        val dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1
        val dailySummaries = (0 until dayCount).map { offset ->
            val date = startDate.plusDays(offset)
            val dayTxns = grouped[date].orEmpty()
            DailySummary(
                date = date,
                income = dayTxns.filter { it.type == "income" }.sumOf { it.amount },
                expense = dayTxns.filter { it.type == "expense" }.sumOf { it.amount },
                transactionCount = dayTxns.size,
            )
        }

        return CustomReport(
            startDate = startDate,
            endDate = endDate,
            totalIncome = dailySummaries.sumOf { it.income },
            totalExpense = dailySummaries.sumOf { it.expense },
            dailySummaries = dailySummaries,
        )
    }
}
