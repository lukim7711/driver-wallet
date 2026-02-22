package com.driverwallet.app.feature.input.ui

import app.cash.turbine.test
import com.driverwallet.app.core.model.Categories
import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.core.test.MainDispatcherExtension
import com.driverwallet.app.core.ui.navigation.GlobalUiEvent
import com.driverwallet.app.feature.input.domain.SaveTransactionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
class QuickInputViewModelTest {

    private val saveUseCase: SaveTransactionUseCase = mockk()
    private lateinit var vm: QuickInputViewModel

    @BeforeEach
    fun setup() {
        coEvery { saveUseCase(any()) } returns Result.success(Unit)
        vm = QuickInputViewModel(saveUseCase)
    }

    private val readyState get() = vm.uiState.value as QuickInputUiState.Ready

    @Nested
    @DisplayName("SwitchType")
    inner class SwitchType {

        @Test
        fun `initial state is INCOME with income categories`() {
            assertEquals(TransactionType.INCOME, readyState.type)
            assertEquals(Categories.incomeCategories, readyState.categories)
        }

        @Test
        fun `switch to EXPENSE shows expense categories and resets category`() {
            vm.onAction(QuickInputUiAction.SelectCategory(Categories.ORDER))
            vm.onAction(QuickInputUiAction.SwitchType(TransactionType.EXPENSE))

            assertEquals(TransactionType.EXPENSE, readyState.type)
            assertEquals(Categories.expenseCategories, readyState.categories)
            assertNull(readyState.selectedCategory)
        }
    }

    @Nested
    @DisplayName("AppendDigit + Backspace")
    inner class NumberPad {

        @Test
        fun `append single digit builds amount`() {
            vm.onAction(QuickInputUiAction.AppendDigit("5"))
            assertEquals(5L, readyState.amount)
        }

        @Test
        fun `append 000 adds three digits`() {
            vm.onAction(QuickInputUiAction.AppendDigit("5"))
            vm.onAction(QuickInputUiAction.AppendDigit("000"))
            assertEquals(5_000L, readyState.amount)
        }

        @Test
        fun `backspace removes last digit`() {
            vm.onAction(QuickInputUiAction.AppendDigit("1"))
            vm.onAction(QuickInputUiAction.AppendDigit("5"))
            vm.onAction(QuickInputUiAction.Backspace)
            assertEquals(1L, readyState.amount)
        }

        @Test
        fun `amount capped at 9 digits`() {
            repeat(9) { vm.onAction(QuickInputUiAction.AppendDigit("9")) }
            val before = readyState.amount
            vm.onAction(QuickInputUiAction.AppendDigit("1"))
            assertEquals(before, readyState.amount)
        }
    }

    @Nested
    @DisplayName("AddPreset")
    inner class Preset {

        @Test
        fun `preset is additive not replacement`() {
            vm.onAction(QuickInputUiAction.AppendDigit("5"))
            vm.onAction(QuickInputUiAction.AppendDigit("000"))
            vm.onAction(QuickInputUiAction.AddPreset(10_000))
            assertEquals(15_000L, readyState.amount)
        }
    }

    @Nested
    @DisplayName("canSave")
    inner class CanSave {

        @Test
        fun `false when amount is 0`() {
            vm.onAction(QuickInputUiAction.SelectCategory(Categories.FUEL))
            assertFalse(readyState.canSave)
        }

        @Test
        fun `false when no category selected`() {
            vm.onAction(QuickInputUiAction.AppendDigit("5"))
            assertFalse(readyState.canSave)
        }

        @Test
        fun `true when amount gt 0 and category selected`() {
            vm.onAction(QuickInputUiAction.AppendDigit("5"))
            vm.onAction(QuickInputUiAction.SelectCategory(Categories.FUEL))
            assertTrue(readyState.canSave)
        }
    }

    @Nested
    @DisplayName("Save")
    inner class Save {

        @Test
        fun `success emits snackbar and resets form`() = runTest {
            vm.onAction(QuickInputUiAction.AppendDigit("5"))
            vm.onAction(QuickInputUiAction.SelectCategory(Categories.FUEL))

            vm.uiEvent.test {
                vm.onAction(QuickInputUiAction.Save)
                val event = awaitItem()
                assertTrue(event is GlobalUiEvent.ShowSnackbar)
                assertTrue((event as GlobalUiEvent.ShowSnackbar).message.contains("tersimpan"))
            }

            assertEquals(0L, readyState.amount)
            assertNull(readyState.selectedCategory)
            coVerify(exactly = 1) { saveUseCase(any()) }
        }

        @Test
        fun `failure emits error snackbar`() = runTest {
            coEvery { saveUseCase(any()) } returns Result.failure(Exception("DB error"))
            vm.onAction(QuickInputUiAction.AppendDigit("5"))
            vm.onAction(QuickInputUiAction.SelectCategory(Categories.FUEL))

            vm.uiEvent.test {
                vm.onAction(QuickInputUiAction.Save)
                val event = awaitItem() as GlobalUiEvent.ShowSnackbar
                assertEquals("DB error", event.message)
            }
        }
    }
}
