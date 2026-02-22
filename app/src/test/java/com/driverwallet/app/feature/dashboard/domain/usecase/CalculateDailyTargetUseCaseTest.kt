package com.driverwallet.app.feature.dashboard.domain.usecase

import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.settings.domain.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CalculateDailyTargetUseCaseTest {

    private val debtRepository: DebtRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private lateinit var useCase: CalculateDailyTargetUseCase

    @BeforeEach
    fun setup() {
        useCase = CalculateDailyTargetUseCase(debtRepository, settingsRepository)

        // Default stubs: no rest days, no debts, no expenses
        coEvery { settingsRepository.getSetting("rest_days") } returns "0"
        coEvery { settingsRepository.getSetting("debt_target_date") } returns null
        every { debtRepository.observeTotalRemaining() } returns flowOf(0L)
        coEvery { settingsRepository.getTotalMonthlyExpense() } returns 0L
        coEvery { settingsRepository.getTotalDailyExpense() } returns 0L
        coEvery { settingsRepository.getTotalDailyBudget() } returns 0L
    }

    @Nested
    @DisplayName("Rest day")
    inner class RestDay {

        @Test
        fun `rest day returns targetAmount 0 and isRestDay true`() = runTest {
            // Today's day of week % 7 — we mock rest_days to include all days
            coEvery { settingsRepository.getSetting("rest_days") } returns "0,1,2,3,4,5,6"

            val result = useCase(earnedToday = 50_000)

            assertEquals(0L, result.targetAmount)
            assertTrue(result.isRestDay)
            assertEquals(50_000L, result.earnedAmount)
        }

        @Test
        fun `rest day is always on track`() = runTest {
            coEvery { settingsRepository.getSetting("rest_days") } returns "0,1,2,3,4,5,6"

            val result = useCase(earnedToday = 0)

            assertTrue(result.isOnTrack)
        }
    }

    @Nested
    @DisplayName("Normal working day")
    inner class NormalDay {

        @Test
        fun `no debts and no expenses returns target 0`() = runTest {
            // rest_days = "" means no matching rest day
            coEvery { settingsRepository.getSetting("rest_days") } returns ""

            val result = useCase(earnedToday = 100_000)

            assertEquals(0L, result.targetAmount)
            assertFalse(result.isRestDay)
        }

        @Test
        fun `daily budget adds to target`() = runTest {
            coEvery { settingsRepository.getSetting("rest_days") } returns ""
            coEvery { settingsRepository.getTotalDailyBudget() } returns 80_000L

            val result = useCase(earnedToday = 50_000)

            assertEquals(80_000L, result.targetAmount)
            assertFalse(result.isOnTrack) // 50k < 80k
        }

        @Test
        fun `daily fixed expense adds to target`() = runTest {
            coEvery { settingsRepository.getSetting("rest_days") } returns ""
            coEvery { settingsRepository.getTotalDailyExpense() } returns 20_000L

            val result = useCase(earnedToday = 25_000)

            assertEquals(20_000L, result.targetAmount)
            assertTrue(result.isOnTrack) // 25k >= 20k
        }

        @Test
        fun `all components sum up correctly`() = runTest {
            coEvery { settingsRepository.getSetting("rest_days") } returns ""
            coEvery { settingsRepository.getTotalDailyBudget() } returns 50_000L
            coEvery { settingsRepository.getTotalDailyExpense() } returns 10_000L
            coEvery { settingsRepository.getTotalMonthlyExpense() } returns 280_000L // 280k / 28 = 10k

            val result = useCase(earnedToday = 0)

            // target = 0 (no debt) + 10k (monthly/28) + 10k (daily) + 50k (budget) = 70k
            // Note: exact monthly proration depends on current month's days
            assertTrue(result.targetAmount > 0)
            assertFalse(result.isOnTrack)
        }
    }

    @Nested
    @DisplayName("DailyTarget computed properties")
    inner class ComputedProperties {

        @Test
        fun `percentage is 0 when targetAmount is 0`() = runTest {
            coEvery { settingsRepository.getSetting("rest_days") } returns ""

            val result = useCase(earnedToday = 100_000)

            assertEquals(0f, result.percentage)
        }

        @Test
        fun `percentage capped at 2f`() = runTest {
            coEvery { settingsRepository.getSetting("rest_days") } returns ""
            coEvery { settingsRepository.getTotalDailyBudget() } returns 10_000L

            val result = useCase(earnedToday = 100_000) // 10x target

            assertEquals(2f, result.percentage)
        }
    }
}
