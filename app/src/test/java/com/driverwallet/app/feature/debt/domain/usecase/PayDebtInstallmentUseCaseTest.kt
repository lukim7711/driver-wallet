package com.driverwallet.app.feature.debt.domain.usecase

import com.driverwallet.app.feature.debt.domain.DebtRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PayDebtInstallmentUseCaseTest {

    private val repository: DebtRepository = mockk()
    private lateinit var useCase: PayDebtInstallmentUseCase

    @BeforeEach
    fun setup() {
        useCase = PayDebtInstallmentUseCase(repository)
        coEvery { repository.payInstallment(any(), any(), any()) } just runs
    }

    @Nested
    @DisplayName("Validation")
    inner class Validation {

        @Test
        fun `amount 0 returns failure`() = runTest {
            val result = useCase("debt-1", "schedule-1", 0)

            assertTrue(result.isFailure)
            assertEquals(
                "Jumlah bayar harus lebih dari 0",
                result.exceptionOrNull()?.message,
            )
        }

        @Test
        fun `negative amount returns failure`() = runTest {
            val result = useCase("debt-1", "schedule-1", -5_000)

            assertTrue(result.isFailure)
            assertEquals(
                "Jumlah bayar harus lebih dari 0",
                result.exceptionOrNull()?.message,
            )
        }

        @Test
        fun `amount 0 does not call repository`() = runTest {
            useCase("debt-1", "schedule-1", 0)

            coVerify(exactly = 0) { repository.payInstallment(any(), any(), any()) }
        }
    }

    @Nested
    @DisplayName("Happy path")
    inner class HappyPath {

        @Test
        fun `valid amount calls repository payInstallment`() = runTest {
            val result = useCase("debt-1", "schedule-1", 500_000)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                repository.payInstallment("debt-1", "schedule-1", 500_000)
            }
        }

        @Test
        fun `amount 1 is accepted as minimum valid`() = runTest {
            val result = useCase("debt-1", "schedule-1", 1)

            assertTrue(result.isSuccess)
        }

        @Test
        fun `large amount is accepted`() = runTest {
            val result = useCase("debt-1", "schedule-1", 999_999_999)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                repository.payInstallment("debt-1", "schedule-1", 999_999_999)
            }
        }
    }

    @Nested
    @DisplayName("Repository error")
    inner class RepositoryError {

        @Test
        fun `repository exception wrapped in Result failure`() = runTest {
            coEvery {
                repository.payInstallment(any(), any(), any())
            } throws RuntimeException("DB error")

            val result = useCase("debt-1", "schedule-1", 500_000)

            assertTrue(result.isFailure)
            assertEquals("DB error", result.exceptionOrNull()?.message)
        }
    }
}
