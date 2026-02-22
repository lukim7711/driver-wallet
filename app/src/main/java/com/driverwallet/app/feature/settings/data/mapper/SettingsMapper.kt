package com.driverwallet.app.feature.settings.data.mapper

import com.driverwallet.app.feature.settings.data.entity.DailyBudgetEntity
import com.driverwallet.app.feature.settings.data.entity.DailyExpenseEntity
import com.driverwallet.app.feature.settings.data.entity.MonthlyExpenseEntity
import com.driverwallet.app.feature.settings.data.entity.RecurringExpenseEntity
import com.driverwallet.app.feature.settings.domain.model.DailyBudget
import com.driverwallet.app.feature.settings.domain.model.DailyExpense
import com.driverwallet.app.feature.settings.domain.model.MonthlyExpense
import com.driverwallet.app.feature.settings.domain.model.RecurringExpense
import com.driverwallet.app.feature.settings.domain.model.RecurringFrequency

// ── Legacy mappers (kept for backward compat, will be removed) ──

fun DailyBudgetEntity.toDomain() = DailyBudget(
    id = id,
    category = category,
    amount = amount,
)

fun MonthlyExpenseEntity.toDomain() = MonthlyExpense(
    id = id,
    name = name,
    icon = icon,
    amount = amount,
)

fun DailyExpenseEntity.toDomain() = DailyExpense(
    id = id,
    name = name,
    icon = icon,
    amount = amount,
)

fun DailyBudget.toEntity() = DailyBudgetEntity(
    id = id,
    category = category,
    amount = amount,
)

fun MonthlyExpense.toEntity() = MonthlyExpenseEntity(
    id = id,
    name = name,
    icon = icon,
    amount = amount,
)

fun DailyExpense.toEntity() = DailyExpenseEntity(
    id = id,
    name = name,
    icon = icon,
    amount = amount,
)

// ── New unified mappers ──

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
