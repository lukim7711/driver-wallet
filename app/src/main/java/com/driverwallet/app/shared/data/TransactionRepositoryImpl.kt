package com.driverwallet.app.shared.data

import com.driverwallet.app.core.model.Categories
import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.core.model.nowJakarta
import com.driverwallet.app.core.model.todayJakarta
import com.driverwallet.app.shared.data.dao.TransactionDao
import com.driverwallet.app.shared.data.mapper.toDomain
import com.driverwallet.app.shared.data.mapper.toEntity
import com.driverwallet.app.shared.domain.model.CategorySummary
import com.driverwallet.app.shared.domain.model.DailySummary
import com.driverwallet.app.shared.domain.model.TodaySummary
import com.driverwallet.app.shared.domain.model.Transaction
import com.driverwallet.app.shared.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    override fun observeTodayTransactions(): Flow<List<Transaction>> {
        val todayPrefix = todayJakarta().toString()
        return transactionDao.observeByDate(todayPrefix).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTodaySummary(todayPrefix: String): TodaySummary {
        val tuples = transactionDao.getTodaySummary(todayPrefix)
        var income = 0L
        var totalExpense = 0L
        tuples.forEach { tuple ->
            when (tuple.type) {
                TransactionType.INCOME.name -> income = tuple.total
                TransactionType.EXPENSE.name -> totalExpense = tuple.total
            }
        }
        // Separate debt_payment from regular expense
        val debtPayment = transactionDao.getBudgetSpentToday(
            todayPrefix,
            listOf(Categories.DEBT_PAYMENT.key),
        )
        return TodaySummary(
            income = income,
            expense = totalExpense - debtPayment,
            debtPayment = debtPayment,
        )
    }

    override suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction.toEntity())
    }

    override suspend fun getDailySummary(
        startDate: String,
        endDate: String,
    ): List<DailySummary> {
        val tuples = transactionDao.getDailySummary(startDate, endDate)
        return tuples.groupBy { it.date }.map { (date, entries) ->
            var income = 0L
            var expense = 0L
            var count = 0
            entries.forEach { entry ->
                count += entry.count
                when (entry.type) {
                    TransactionType.INCOME.name -> income = entry.total
                    TransactionType.EXPENSE.name -> expense = entry.total
                }
            }
            DailySummary(
                date = date,
                income = income,
                expense = expense,
                transactionCount = count,
            )
        }
    }

    override suspend fun getCategorySummary(
        startDate: String,
        endDate: String,
    ): List<CategorySummary> {
        val tuples = transactionDao.getCategorySummary(startDate, endDate)
        val totalAmount = tuples.sumOf { it.total }.coerceAtLeast(1)
        return tuples.map { tuple ->
            val category = Categories.fromKey(tuple.category)
            CategorySummary(
                categoryKey = tuple.category,
                categoryLabel = category?.label ?: tuple.category,
                total = tuple.total,
                count = tuple.count,
                percentage = tuple.total.toFloat() / totalAmount,
            )
        }
    }

    override suspend fun getByDateRange(
        startDate: String,
        endDate: String,
    ): List<Transaction> =
        transactionDao.getByDateRange(startDate, endDate).map { it.toDomain() }

    override suspend fun getBudgetSpentToday(
        todayPrefix: String,
        categories: List<String>,
    ): Long = transactionDao.getBudgetSpentToday(todayPrefix, categories)

    override suspend fun softDelete(id: String) {
        val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        transactionDao.softDelete(id, now)
    }
}
