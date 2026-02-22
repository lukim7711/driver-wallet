package com.driverwallet.app.feature.debt.data.mapper

import com.driverwallet.app.feature.debt.data.dao.UpcomingDueTuple
import com.driverwallet.app.feature.debt.data.entity.DebtEntity
import com.driverwallet.app.feature.debt.data.entity.DebtScheduleEntity
import com.driverwallet.app.feature.debt.domain.model.Debt
import com.driverwallet.app.feature.debt.domain.model.DebtSchedule
import com.driverwallet.app.feature.debt.domain.model.UpcomingDue

// Entity → Domain
fun DebtEntity.toDomain() = Debt(
    id = id,
    platform = platform,
    name = name,
    totalAmount = totalAmount,
    remainingAmount = remainingAmount,
    installmentPerMonth = installmentPerMonth,
    installmentCount = installmentCount,
    dueDay = dueDay,
    interestRate = interestRate,
    penaltyType = penaltyType,
    penaltyRate = penaltyRate,
    debtType = debtType,
    note = note,
    status = status,
    startDate = startDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun DebtScheduleEntity.toDomain() = DebtSchedule(
    id = id,
    debtId = debtId,
    installmentNumber = installmentNumber,
    dueDate = dueDate,
    expectedAmount = expectedAmount,
    actualAmount = actualAmount,
    status = status,
    paidAt = paidAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun UpcomingDueTuple.toDomain() = UpcomingDue(
    debtId = debtId,
    debtName = debtName,
    platform = platform,
    dueDate = dueDate,
    expectedAmount = expectedAmount,
    installmentNumber = installmentNumber,
)

// Domain → Entity
fun Debt.toEntity() = DebtEntity(
    id = id,
    platform = platform,
    name = name,
    totalAmount = totalAmount,
    remainingAmount = remainingAmount,
    installmentPerMonth = installmentPerMonth,
    installmentCount = installmentCount,
    dueDay = dueDay,
    interestRate = interestRate,
    penaltyType = penaltyType,
    penaltyRate = penaltyRate,
    debtType = debtType,
    note = note,
    status = status,
    startDate = startDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun DebtSchedule.toEntity() = DebtScheduleEntity(
    id = id,
    debtId = debtId,
    installmentNumber = installmentNumber,
    dueDate = dueDate,
    expectedAmount = expectedAmount,
    actualAmount = actualAmount,
    status = status,
    paidAt = paidAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
