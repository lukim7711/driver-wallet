package com.driverwallet.app.feature.settings.data.mapper

import com.driverwallet.app.feature.settings.data.entity.RecurringExpenseEntity
import com.driverwallet.app.feature.settings.domain.model.RecurringExpense
import com.driverwallet.app.feature.settings.domain.model.RecurringFrequency

fun RecurringExpenseEntity.toDomain() = RecurringExpense(
    id = id,
    name = name,
    icon = icon,
    amount = amount,
    frequency = RecurringFrequency.fromValue(frequency),
)

fun RecurringExpense.toEntity() = RecurringExpenseEntity(
    id = id,
    name = name,
    icon = icon,
    amount = amount,
    frequency = frequency.value,
)
