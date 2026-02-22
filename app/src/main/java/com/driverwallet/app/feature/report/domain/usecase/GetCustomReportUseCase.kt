package com.driverwallet.app.feature.report.domain.usecase

import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.feature.report.domain.model.CustomReport
import com.driverwallet.app.feature.report.domain.model.DailySummary
import com.driverwallet.app.shared.data.repository.TransactionRepository
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
            it.createdAt.substring(0, 10)
        }

        val dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1
        val dailySummaries = (0 until dayCount).map { offset ->
            val date = startDate.plusDays(offset)
            val dateStr = date.toString()
            val dayTxns = grouped[dateStr].orEmpty()
            DailySummary(
                date = dateStr,
                income = dayTxns
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount },
                expense = dayTxns
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount },
                transactionCount = dayTxns.size,
            )
        }

        return CustomReport(
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            totalIncome = dailySummaries.sumOf { it.income },
            totalExpense = dailySummaries.sumOf { it.expense },
            dailySummaries = dailySummaries,
        )
    }
}
