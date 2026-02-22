package com.driverwallet.app.feature.dashboard.ui

import com.driverwallet.app.core.test.MainDispatcherExtension
import com.driverwallet.app.feature.dashboard.domain.model.BudgetInfo
import com.driverwallet.app.feature.dashboard.domain.model.DailyTarget
import com.driverwallet.app.feature.dashboard.domain.model.DashboardData
import com.driverwallet.app.feature.dashboard.domain.model.TodaySummary
import com.driverwallet.app.feature.dashboard.domain.usecase.GetDashboardSummaryUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
class DashboardViewModelTest {

    private val getDashboardSummary: GetDashboardSummaryUseCase = mockk()

    private fun createVm() = DashboardViewModel(getDashboardSummary)

    private fun dashboardData(
        income: Long = 200_000,
        expense: Long = 50_000,
        yesterdayProfit: Long? = 100_000,
    ) = DashboardData(
        todaySummary = TodaySummary(income = income, expense = expense),
        dailyTarget = DailyTarget(),
        budgetInfo = BudgetInfo(),
        yesterdayProfit = yesterdayProfit,
    )

    @Nested
    @DisplayName("Loading to Success")
    inner class LoadSuccess {

        @Test
        fun `init loads dashboard into Success state`() = runTest {
            coEvery { getDashboardSummary() } returns dashboardData()

            val vm = createVm()
            val state = vm.uiState.value

            assertTrue(state is DashboardUiState.Success)
            val success = state as DashboardUiState.Success
            assertEquals(200_000L, success.todaySummary.income)
        }

        @Test
        fun `percent change calculated from yesterday profit`() = runTest {
            coEvery { getDashboardSummary() } returns dashboardData()

            val vm = createVm()
            val success = vm.uiState.value as DashboardUiState.Success

            assertNotNull(success.percentChange)
            assertEquals(50f, success.percentChange!!, 0.1f)
        }

        @Test
        fun `percent change null when yesterday profit is null`() = runTest {
            coEvery { getDashboardSummary() } returns dashboardData(yesterdayProfit = null)

            val vm = createVm()
            val success = vm.uiState.value as DashboardUiState.Success

            assertNull(success.percentChange)
        }

        @Test
        fun `percent change null when yesterday profit is 0`() = runTest {
            coEvery { getDashboardSummary() } returns dashboardData(yesterdayProfit = 0)

            val vm = createVm()
            val success = vm.uiState.value as DashboardUiState.Success

            assertNull(success.percentChange)
        }
    }

    @Nested
    @DisplayName("Error")
    inner class LoadError {

        @Test
        fun `exception maps to Error state`() = runTest {
            coEvery { getDashboardSummary() } throws RuntimeException("Network error")

            val vm = createVm()

            assertTrue(vm.uiState.value is DashboardUiState.Error)
            assertEquals(
                "Network error",
                (vm.uiState.value as DashboardUiState.Error).message,
            )
        }
    }

    @Nested
    @DisplayName("Refresh")
    inner class Refresh {

        @Test
        fun `refresh reloads data`() = runTest {
            coEvery { getDashboardSummary() } returns dashboardData()

            val vm = createVm()
            vm.onAction(DashboardUiAction.Refresh)

            coVerify(exactly = 2) { getDashboardSummary() }
        }
    }
}
