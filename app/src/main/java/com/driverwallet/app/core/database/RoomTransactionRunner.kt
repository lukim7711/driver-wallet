package com.driverwallet.app.core.database

import androidx.room.withTransaction
import javax.inject.Inject

class RoomTransactionRunner @Inject constructor(
    private val database: AppDatabase,
) : TransactionRunner {

    override suspend fun <T> withTransaction(block: suspend () -> T): T =
        database.withTransaction(block)
}
