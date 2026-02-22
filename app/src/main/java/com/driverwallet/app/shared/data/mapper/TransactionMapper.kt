package com.driverwallet.app.shared.data.mapper

import com.driverwallet.app.core.model.Categories
import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.core.model.nowJakarta
import com.driverwallet.app.core.util.UuidGenerator
import com.driverwallet.app.shared.data.entity.TransactionEntity
import com.driverwallet.app.shared.domain.model.Transaction
import java.time.format.DateTimeFormatter

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.INCOME),
    category = Categories.fromKey(category),
    amount = amount,
    note = note,
    debtId = debtId,
    createdAt = createdAt,
)

fun Transaction.toEntity(): TransactionEntity {
    val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
    return TransactionEntity(
        id = id.ifEmpty { UuidGenerator.generate() },
        type = type.name,
        category = category?.key.orEmpty(),
        amount = amount,
        note = note,
        debtId = debtId,
        createdAt = createdAt.ifEmpty { now },
        updatedAt = now,
    )
}
