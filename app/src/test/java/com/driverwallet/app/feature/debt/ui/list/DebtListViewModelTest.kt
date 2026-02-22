package com.driverwallet.app.feature.debt.ui.list

import app.cash.turbine.test
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.feature.debt.domain.DebtRepository
import com.driverwallet.app.feature.debt.domain.usecase.GetActiveDebtsUseCase
import com.driverwallet.app.feature.debt.domain.usecase.PayDebtInstallmentUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DebtListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getActiveDebts: GetActiveDebtsUseCase = mockk()
    private val payInstallment: PayDebtInstallmentUseCase = mockk()
    private val debtRepository: DebtRepository = mockk()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("Observe debts")
    inner class ObserveDebts {

        @Test
        fun `empty debts results in Empty state`() = runTest(testDispatcher) {
            every { getActiveDebts() } returns flowOf(emptyList())

            val vm = DebtListViewModel(getActiveDebts, payInstallment, debtRepository)

            assertTrue(vm.uiState.value is DebtListUiState.Empty)
        }
    }

    @Nested
    @DisplayName("Payment")
    inner class Payment {

        @Test
        fun `confirm payment success dismisses dialog and shows snackbar`() = runTest(testDispatcher) {
            every { getActiveDebts() } returns flowOf(emptyList())
            coEvery { payInstallment(any(), any(), any()) } returns Result.success(Unit)

            val vm = DebtListViewModel(getActiveDebts, payInstallment, debtRepository)

            vm.uiEvent.test {
                vm.onAction(
                    DebtListUiAction.ConfirmPayment(
                        debtId = "debt-1",
                        scheduleId = "schedule-1",
                        amount = 500_000L,
                    ),
                )
                val event = awaitItem() as GlobalUiEvent.ShowSnackbar
                assertTrue(event.message.contains("berhasil dibayar"))
            }
            assertNull(vm.paymentDialog.value)
        }

        @Test
        fun `confirm payment failure shows error snackbar`() = runTest(testDispatcher) {
            every { getActiveDebts() } returns flowOf(emptyList())
            coEvery { payInstallment(any(), any(), any()) } returns Result.failure(
                Exception("Saldo kurang"),
            )

            val vm = DebtListViewModel(getActiveDebts, payInstallment, debtRepository)

            vm.uiEvent.test {
                vm.onAction(
                    DebtListUiAction.ConfirmPayment(
                        debtId = "debt-1",
                        scheduleId = "schedule-1",
                        amount = 500_000L,
                    ),
                )
                val event = awaitItem() as GlobalUiEvent.ShowSnackbar
                assertEquals("Saldo kurang", event.message)
            }
        }
    }

    @Nested
    @DisplayName("Delete")
    inner class Delete {

        @Test
        fun `delete debt calls softDelete and shows snackbar`() = runTest(testDispatcher) {
            every { getActiveDebts() } returns flowOf(emptyList())
            coEvery { debtRepository.softDelete(any()) } just runs

            val vm = DebtListViewModel(getActiveDebts, payInstallment, debtRepository)

            vm.uiEvent.test {
                vm.onAction(DebtListUiAction.DeleteDebt("debt-1"))
                val event = awaitItem() as GlobalUiEvent.ShowSnackbar
                assertEquals("Hutang dihapus", event.message)
            }
            coVerify(exactly = 1) { debtRepository.softDelete("debt-1") }
        }

        @Test
        fun `delete failure shows error snackbar`() = runTest(testDispatcher) {
            every { getActiveDebts() } returns flowOf(emptyList())
            coEvery { debtRepository.softDelete(any()) } throws RuntimeException("DB locked")

            val vm = DebtListViewModel(getActiveDebts, payInstallment, debtRepository)

            vm.uiEvent.test {
                vm.onAction(DebtListUiAction.DeleteDebt("debt-1"))
                val event = awaitItem() as GlobalUiEvent.ShowSnackbar
                assertEquals("DB locked", event.message)
            }
        }
    }
}
