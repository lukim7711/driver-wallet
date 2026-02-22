package com.driverwallet.app.shared.domain.repository

import com.driverwallet.app.shared.domain.model.CategorySummary
import com.driverwallet.app.shared.domain.model.DailySummary
import com.driverwallet.app.shared.domain.model.TodaySummary
import com.driverwallet.app.shared.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Domain-layer contract for transaction data access.
 * No framework (Room/Android) dependencies — pure Kotlin + coroutines.
 */
interface TransactionRepository {
    fun observeTodayTransactions(): Flow<List<Transaction>>
    suspend fun getTodaySummary(todayPrefix: String): TodaySummary
    suspend fun insert(transaction: Transaction)
    suspend fun getDailySummary(startDate: String, endDate: String): List<DailySummary>
    suspend fun getCategorySummary(startDate: String, endDate: String): List<CategorySummary>
    suspend fun getByDateRange(startDate: String, endDate: String): List<Transaction>
    suspend fun getBudgetSpentToday(todayPrefix: String, categories: List<String>): Long
    suspend fun softDelete(id: String)
}
