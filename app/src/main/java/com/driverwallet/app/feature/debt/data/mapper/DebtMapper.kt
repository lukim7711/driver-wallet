package com.driverwallet.app.feature.debt.data.mapper

import com.driverwallet.app.feature.debt.data.dao.UpcomingDueTuple
import com.driverwallet.app.feature.debt.data.entity.DebtEntity
import com.driverwallet.app.feature.debt.data.entity.DebtScheduleEntity
import com.driverwallet.app.feature.debt.domain.model.Debt
import com.driverwallet.app.feature.debt.domain.model.DebtSchedule
import com.driverwallet.app.feature.debt.domain.model.DebtStatus
import com.driverwallet.app.feature.debt.domain.model.DebtType
import com.driverwallet.app.feature.debt.domain.model.PenaltyType
import com.driverwallet.app.feature.debt.domain.model.ScheduleStatus
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
    penaltyType = PenaltyType.fromValue(penaltyType),
    penaltyRate = penaltyRate,
    debtType = DebtType.fromValue(debtType),
    note = note,
    status = DebtStatus.fromValue(status),
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
    status = ScheduleStatus.fromValue(status),
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
    penaltyType = penaltyType.value,
    penaltyRate = penaltyRate,
    debtType = debtType.value,
    note = note,
    status = status.value,
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
    status = status.value,
    paidAt = paidAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
