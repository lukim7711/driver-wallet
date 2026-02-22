package com.driverwallet.app.feature.report.domain.model

import java.time.LocalDate

data class DailySummary(
    val date: LocalDate,
    val income: Long,
    val expense: Long,
    val transactionCount: Int,
) {
    val profit: Long get() = income - expense
}

data class WeeklyReport(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dailySummaries: List<DailySummary>,
    val totalIncome: Long,
    val totalExpense: Long,
) {
    val totalProfit: Long get() = totalIncome - totalExpense
}

data class CategorySummary(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val total: Long,
    val percentage: Float,
    val transactionCount: Int,
)

data class MonthlyReport(
    val year: Int,
    val month: Int,
    val totalIncome: Long,
    val totalExpense: Long,
    val incomeByCategory: List<CategorySummary>,
    val expenseByCategory: List<CategorySummary>,
) {
    val totalProfit: Long get() = totalIncome - totalExpense
}

data class CustomReport(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalIncome: Long,
    val totalExpense: Long,
    val dailySummaries: List<DailySummary>,
) {
    val totalProfit: Long get() = totalIncome - totalExpense
}
