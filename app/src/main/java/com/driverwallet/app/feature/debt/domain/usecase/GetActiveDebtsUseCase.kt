package com.driverwallet.app.feature.debt.domain.usecase

import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.debt.domain.DebtWithScheduleInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveDebtsUseCase @Inject constructor(
    private val repository: DebtRepository,
) {
    operator fun invoke(): Flow<List<DebtWithScheduleInfo>> =
        repository.observeActiveDebtsWithSchedule()
}
