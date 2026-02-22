package com.driverwallet.app.feature.input.domain

import com.driverwallet.app.core.model.Categories
import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.shared.data.repository.TransactionRepository
import com.driverwallet.app.shared.domain.model.Transaction
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

class SaveTransactionUseCaseTest {

    private val repository: TransactionRepository = mockk()
    private lateinit var useCase: SaveTransactionUseCase

    @BeforeEach
    fun setup() {
        useCase = SaveTransactionUseCase(repository)
        coEvery { repository.insert(any()) } just runs
    }

    private fun validTransaction(
        amount: Long = 50_000L,
        category: com.driverwallet.app.core.model.Category? = Categories.FUEL,
        note: String = "BBM Pertamax",
    ) = Transaction(
        type = TransactionType.EXPENSE,
        category = category,
        amount = amount,
        note = note,
    )

    @Nested
    @DisplayName("Validation: amount")
    inner class AmountValidation {

        @Test
        fun `amount 0 returns failure`() = runTest {
            val result = useCase(validTransaction(amount = 0))

            assertTrue(result.isFailure)
            assertEquals(
                "Jumlah harus lebih dari 0",
                result.exceptionOrNull()?.message,
            )
        }

        @Test
        fun `negative amount returns failure`() = runTest {
            val result = useCase(validTransaction(amount = -1_000))

            assertTrue(result.isFailure)
        }

        @Test
        fun `amount exceeds max returns failure`() = runTest {
            val result = useCase(validTransaction(amount = 1_000_000_000))

            assertTrue(result.isFailure)
            assertEquals(
                "Jumlah maksimal Rp 999.999.999",
                result.exceptionOrNull()?.message,
            )
        }

        @Test
        fun `max amount is accepted`() = runTest {
            val result = useCase(validTransaction(amount = 999_999_999))

            assertTrue(result.isSuccess)
        }

        @Test
        fun `amount 1 is accepted`() = runTest {
            val result = useCase(validTransaction(amount = 1))

            assertTrue(result.isSuccess)
        }
    }

    @Nested
    @DisplayName("Validation: note")
    inner class NoteValidation {

        @Test
        fun `note over 100 chars returns failure`() = runTest {
            val longNote = "a".repeat(101)
            val result = useCase(validTransaction(note = longNote))

            assertTrue(result.isFailure)
            assertEquals(
                "Catatan maksimal 100 karakter",
                result.exceptionOrNull()?.message,
            )
        }

        @Test
        fun `note exactly 100 chars is accepted`() = runTest {
            val note100 = "a".repeat(100)
            val result = useCase(validTransaction(note = note100))

            assertTrue(result.isSuccess)
        }

        @Test
        fun `empty note is accepted`() = runTest {
            val result = useCase(validTransaction(note = ""))

            assertTrue(result.isSuccess)
        }
    }

    @Nested
    @DisplayName("Validation: category")
    inner class CategoryValidation {

        @Test
        fun `null category returns failure`() = runTest {
            val result = useCase(validTransaction(category = null))

            assertTrue(result.isFailure)
            assertEquals(
                "Pilih kategori",
                result.exceptionOrNull()?.message,
            )
        }
    }

    @Nested
    @DisplayName("Happy path")
    inner class HappyPath {

        @Test
        fun `valid transaction calls repository insert`() = runTest {
            val tx = validTransaction()

            val result = useCase(tx)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { repository.insert(tx) }
        }

        @Test
        fun `repository exception is wrapped in Result failure`() = runTest {
            coEvery { repository.insert(any()) } throws RuntimeException("DB error")

            val result = useCase(validTransaction())

            assertTrue(result.isFailure)
            assertEquals("DB error", result.exceptionOrNull()?.message)
        }
    }
}
