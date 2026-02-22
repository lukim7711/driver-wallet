package com.driverwallet.app.feature.report.domain.usecase

import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.feature.report.domain.model.CategorySummary
import com.driverwallet.app.feature.report.domain.model.MonthlyReport
import com.driverwallet.app.shared.data.repository.TransactionRepository
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

        val incomeByCategory = transactions
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.category?.key ?: "unknown" }
            .map { (catKey, txns) ->
                val total = txns.sumOf { it.amount }
                val firstCat = txns.first().category
                CategorySummary(
                    categoryId = catKey,
                    categoryName = firstCat?.label ?: catKey,
                    categoryIcon = firstCat?.iconName ?: "",
                    total = total,
                    percentage = if (totalIncome > 0) total.toFloat() / totalIncome else 0f,
                    transactionCount = txns.size,
                )
            }
            .sortedByDescending { it.total }

        val expenseByCategory = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category?.key ?: "unknown" }
            .map { (catKey, txns) ->
                val total = txns.sumOf { it.amount }
                val firstCat = txns.first().category
                CategorySummary(
                    categoryId = catKey,
                    categoryName = firstCat?.label ?: catKey,
                    categoryIcon = firstCat?.iconName ?: "",
                    total = total,
                    percentage = if (totalExpense > 0) total.toFloat() / totalExpense else 0f,
                    transactionCount = txns.size,
                )
            }
            .sortedByDescending { it.total }

        return MonthlyReport(
            year = year,
            month = month,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            incomeByCategory = incomeByCategory,
            expenseByCategory = expenseByCategory,
        )
    }
}
