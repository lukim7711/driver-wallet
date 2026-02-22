package com.driverwallet.app.shared.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.driverwallet.app.shared.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class TodaySummaryTuple(val type: String, val total: Long)
data class DailySummaryTuple(val date: String, val type: String, val total: Long, val count: Int)
data class CategorySummaryTuple(val category: String, val total: Long, val count: Int)

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Query("""
        SELECT type, SUM(amount) AS total FROM transactions
        WHERE created_at LIKE :todayPrefix || '%' AND is_deleted = 0
        GROUP BY type
    """)
    suspend fun getTodaySummary(todayPrefix: String): List<TodaySummaryTuple>

    @Query("""
        SELECT * FROM transactions
        WHERE created_at LIKE :datePrefix || '%' AND is_deleted = 0
        ORDER BY created_at DESC LIMIT :limit
    """)
    fun getRecentByDate(datePrefix: String, limit: Int = 5): Flow<List<TransactionEntity>>

    @Query("""
        SELECT substr(created_at, 1, 10) AS date, type, SUM(amount) AS total, COUNT(*) AS count
        FROM transactions
        WHERE created_at >= :startDate AND created_at <= :endDate AND is_deleted = 0
        GROUP BY substr(created_at, 1, 10), type ORDER BY date ASC
    """)
    suspend fun getDailySummary(startDate: String, endDate: String): List<DailySummaryTuple>

    @Query("""
        SELECT category, SUM(amount) AS total, COUNT(*) AS count FROM transactions
        WHERE created_at >= :startDate AND created_at <= :endDate AND is_deleted = 0
        GROUP BY category ORDER BY total DESC
    """)
    suspend fun getCategorySummary(startDate: String, endDate: String): List<CategorySummaryTuple>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'EXPENSE' AND category IN (:categories)
        AND created_at LIKE :todayPrefix || '%' AND is_deleted = 0
    """)
    suspend fun getBudgetSpentToday(todayPrefix: String, categories: List<String>): Long

    @Query("""
        SELECT * FROM transactions
        WHERE created_at >= :startDate AND created_at <= :endDate AND is_deleted = 0
        ORDER BY created_at DESC
    """)
    suspend fun getByDateRange(startDate: String, endDate: String): List<TransactionEntity>

    @Query("UPDATE transactions SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: String)

    @Query("""
        SELECT * FROM transactions
        WHERE created_at LIKE :datePrefix || '%' AND is_deleted = 0
        ORDER BY created_at DESC
    """)
    fun observeByDate(datePrefix: String): Flow<List<TransactionEntity>>
}
