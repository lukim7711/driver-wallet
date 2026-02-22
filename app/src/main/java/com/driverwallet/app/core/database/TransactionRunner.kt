package com.driverwallet.app.core.database

/**
 * Abstraction for atomic database transactions.
 * Prevents repositories from depending on [AppDatabase] directly.
 */
interface TransactionRunner {
    suspend fun <T> withTransaction(block: suspend () -> T): T
}
