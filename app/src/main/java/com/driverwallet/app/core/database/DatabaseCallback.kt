package com.driverwallet.app.core.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        seedDailyBudgets(db)
        seedSettings(db)
    }

    private fun seedDailyBudgets(db: SupportSQLiteDatabase) {
        val budgets = listOf(
            "fuel" to 30_000L,
            "food" to 50_000L,
            "cigarette" to 25_000L,
            "phone" to 5_000L,
        )
        budgets.forEach { (category, amount) ->
            db.insert(
                "daily_budgets",
                SQLiteDatabase.CONFLICT_IGNORE,
                ContentValues().apply {
                    put("category", category)
                    put("amount", amount)
                },
            )
        }
    }

    private fun seedSettings(db: SupportSQLiteDatabase) {
        val settings = listOf(
            "debt_target_date" to "",
            "rest_days" to "0",
        )
        settings.forEach { (key, value) ->
            db.insert(
                "settings",
                SQLiteDatabase.CONFLICT_IGNORE,
                ContentValues().apply {
                    put("key", key)
                    put("value", value)
                },
            )
        }
    }
}
