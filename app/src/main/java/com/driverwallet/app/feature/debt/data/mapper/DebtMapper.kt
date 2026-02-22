package com.driverwallet.app.feature.debt.data.mapper

import com.driverwallet.app.feature.debt.data.dao.UpcomingDueTuple
import com.driverwallet.app.feature.debt.data.entity.DebtEntity
import com.driverwallet.app.feature.debt.data.entity.DebtPaymentEntity
import com.driverwallet.app.feature.debt.data.entity.DebtScheduleEntity
import com.driverwallet.app.feature.debt.data.entity.KasbonEntryEntity
import com.driverwallet.app.feature.debt.domain.model.Debt
import com.driverwallet.app.feature.debt.domain.model.DebtDetail
import com.driverwallet.app.feature.debt.domain.model.DebtPayment
import com.driverwallet.app.feature.debt.domain.model.DebtSchedule
import com.driverwallet.app.feature.debt.domain.model.DebtStatus
import com.driverwallet.app.feature.debt.domain.model.DebtType
import com.driverwallet.app.feature.debt.domain.model.KasbonEntry
import com.driverwallet.app.feature.debt.domain.model.PenaltyType
import com.driverwallet.app.feature.debt.domain.model.ScheduleStatus
import com.driverwallet.app.feature.debt.domain.model.UpcomingDue

// ========== DebtEntity ↔ Debt ==========

fun DebtEntity.toDomain(): Debt {
    val type = DebtType.fromValue(debtType)
    val detail = when (type) {
        DebtType.INSTALLMENT -> DebtDetail.Installment(
            platform = platform,
            installmentPerMonth = installmentPerMonth,
            installmentCount = installmentCount,
            dueDay = dueDay,
            interestRate = interestRate,
            penaltyType = PenaltyType.fromValue(penaltyType),
            penaltyRate = penaltyRate,
        )
        DebtType.PERSONAL -> DebtDetail.Personal(
            borrowerName = borrowerName ?: "",
            relationship = relationship ?: "",
            agreedReturnDate = agreedReturnDate,
        )
        DebtType.TAB -> DebtDetail.Tab(
            merchantName = merchantName ?: "",
            merchantType = merchantType ?: "",
        )
    }
    return Debt(
        id = id,
        name = name,
        debtType = type,
        detail = detail,
        totalAmount = totalAmount,
        remainingAmount = remainingAmount,
        note = note,
        status = DebtStatus.fromValue(status),
        startDate = startDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Debt.toEntity(): DebtEntity {
    val (plat, instPerMonth, instCount, due, interest, penType, penRate,
        bName, rel, agrDate, merName, merType) = flattenDetail()

    return DebtEntity(
        id = id,
        name = name,
        debtType = debtType.value,
        totalAmount = totalAmount,
        remainingAmount = remainingAmount,
        note = note,
        status = status.value,
        startDate = startDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        platform = plat,
        installmentPerMonth = instPerMonth,
        installmentCount = instCount,
        dueDay = due,
        interestRate = interest,
        penaltyType = penType,
        penaltyRate = penRate,
        borrowerName = bName,
        relationship = rel,
        agreedReturnDate = agrDate,
        merchantName = merName,
        merchantType = merType,
    )
}

/**
 * Flatten DebtDetail sealed interface into flat values for entity storage.
 */
private data class FlatDetail(
    val platform: String = "",
    val installmentPerMonth: Long = 0L,
    val installmentCount: Int = 0,
    val dueDay: Int = 0,
    val interestRate: Double = 0.0,
    val penaltyType: String = "none",
    val penaltyRate: Double = 0.0,
    val borrowerName: String? = null,
    val relationship: String? = null,
    val agreedReturnDate: String? = null,
    val merchantName: String? = null,
    val merchantType: String? = null,
)

private fun Debt.flattenDetail(): FlatDetail = when (val d = detail) {
    is DebtDetail.Installment -> FlatDetail(
        platform = d.platform,
        installmentPerMonth = d.installmentPerMonth,
        installmentCount = d.installmentCount,
        dueDay = d.dueDay,
        interestRate = d.interestRate,
        penaltyType = d.penaltyType.value,
        penaltyRate = d.penaltyRate,
    )
    is DebtDetail.Personal -> FlatDetail(
        borrowerName = d.borrowerName,
        relationship = d.relationship,
        agreedReturnDate = d.agreedReturnDate,
    )
    is DebtDetail.Tab -> FlatDetail(
        merchantName = d.merchantName,
        merchantType = d.merchantType,
    )
}

// ========== DebtScheduleEntity ↔ DebtSchedule ==========

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

// ========== KasbonEntryEntity ↔ KasbonEntry ==========

fun KasbonEntryEntity.toDomain() = KasbonEntry(
    id = id,
    debtId = debtId,
    amount = amount,
    note = note,
    createdAt = createdAt,
)

fun KasbonEntry.toEntity() = KasbonEntryEntity(
    id = id,
    debtId = debtId,
    amount = amount,
    note = note,
    createdAt = createdAt,
)

// ========== DebtPaymentEntity ↔ DebtPayment ==========

fun DebtPaymentEntity.toDomain() = DebtPayment(
    id = id,
    debtId = debtId,
    amount = amount,
    note = note,
    paidAt = paidAt,
    createdAt = createdAt,
)

fun DebtPayment.toEntity() = DebtPaymentEntity(
    id = id,
    debtId = debtId,
    amount = amount,
    note = note,
    paidAt = paidAt,
    createdAt = createdAt,
)

// ========== UpcomingDueTuple → UpcomingDue ==========

fun UpcomingDueTuple.toDomain() = UpcomingDue(
    debtId = debtId,
    debtName = debtName,
    platform = platform,
    dueDate = dueDate,
    expectedAmount = expectedAmount,
    installmentNumber = installmentNumber,
)
