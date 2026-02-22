package com.driverwallet.app.feature.report.domain.usecase

import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.feature.report.domain.model.CategorySummary
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

        val transactions = transactionRepository.getByDateRange(startStr, endStr)

        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val totalExpense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val totalAll = totalIncome + totalExpense
        val categoryBreakdown = transactions
            .groupBy { it.category?.key ?: "unknown" }
            .map { (catKey, txns) ->
                val total = txns.sumOf { it.amount }
                val firstCat = txns.first().category
                CategorySummary(
                    categoryKey = catKey,
                    categoryLabel = firstCat?.label ?: catKey,
                    total = total,
                    count = txns.size,
                    percentage = if (totalAll > 0) total.toFloat() / totalAll else 0f,
                )
            }
            .sortedByDescending { it.total }

        return MonthlyReport(
            month = yearMonth.toString(),
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            categoryBreakdown = categoryBreakdown,
        )
    }
}
