package com.driverwallet.app.feature.report.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.driverwallet.app.core.model.Categories
import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.shared.data.repository.TransactionRepository
import com.driverwallet.app.shared.domain.model.Transaction
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

class ExportCsvUseCaseTest {

    private val repository: TransactionRepository = mockk()
    private val context: Context = mockk(relaxed = true)
    private lateinit var useCase: ExportCsvUseCase
    private lateinit var tempDir: File

    @BeforeEach
    fun setup() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "test_exports")
        tempDir.mkdirs()

        every { context.cacheDir } returns tempDir
        every { context.packageName } returns "com.driverwallet.app"

        mockkStatic(FileProvider::class)
        every {
            FileProvider.getUriForFile(any(), any(), any())
        } returns mockk<Uri>()

        useCase = ExportCsvUseCase(repository, context)
    }

    @AfterEach
    fun tearDown() {
        tempDir.deleteRecursively()
        unmockkStatic(FileProvider::class)
    }

    private fun transaction(
        amount: Long = 50_000,
        type: TransactionType = TransactionType.EXPENSE,
        category: com.driverwallet.app.core.model.Category = Categories.FUEL,
        note: String = "Pertamax",
        createdAt: String = "2026-02-20T10:30:00",
    ) = Transaction(
        type = type,
        category = category,
        amount = amount,
        note = note,
        createdAt = createdAt,
    )

    @Nested
    @DisplayName("CSV format")
    inner class CsvFormat {

        @Test
        fun `header row is correct`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns emptyList()

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            val file = File(tempDir, "exports/laporan_2026-02-16_2026-02-22.csv")
            assertTrue(file.exists())
            val lines = file.readLines()
            assertEquals("Tanggal,Tipe,Kategori,Jumlah,Catatan", lines.first())
        }

        @Test
        fun `data rows formatted correctly`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns listOf(
                transaction(),
            )

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            val file = File(tempDir, "exports/laporan_2026-02-16_2026-02-22.csv")
            val lines = file.readLines()
            assertEquals(2, lines.size) // header + 1 data row
            assertEquals("2026-02-20,Pengeluaran,BBM,50000,Pertamax", lines[1])
        }

        @Test
        fun `income type shows Pemasukan`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns listOf(
                transaction(type = TransactionType.INCOME, category = Categories.ORDER),
            )

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            val file = File(tempDir, "exports/laporan_2026-02-16_2026-02-22.csv")
            val lines = file.readLines()
            assertTrue(lines[1].contains("Pemasukan"))
        }
    }

    @Nested
    @DisplayName("Sanitization")
    inner class Sanitization {

        @Test
        fun `comma in note replaced with semicolon`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns listOf(
                transaction(note = "BBM, Pertamax 92"),
            )

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            val file = File(tempDir, "exports/laporan_2026-02-16_2026-02-22.csv")
            val lines = file.readLines()
            assertTrue(lines[1].endsWith("BBM; Pertamax 92"))
        }

        @Test
        fun `newline in note replaced with space`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns listOf(
                transaction(note = "Baris1\nBaris2"),
            )

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            val file = File(tempDir, "exports/laporan_2026-02-16_2026-02-22.csv")
            val lines = file.readLines()
            // After sanitize: "Baris1 Baris2" — no newline breaking CSV
            assertTrue(lines.size == 2) // header + 1 row, not split
        }
    }

    @Nested
    @DisplayName("Empty data")
    inner class EmptyData {

        @Test
        fun `empty transactions produces header only CSV`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns emptyList()

            val result = useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            assertTrue(result.isSuccess)
            val file = File(tempDir, "exports/laporan_2026-02-16_2026-02-22.csv")
            val lines = file.readLines()
            assertEquals(1, lines.size) // header only
        }
    }

    @Nested
    @DisplayName("Date range query")
    inner class DateRange {

        @Test
        fun `passes correct date range to repository`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns emptyList()

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            io.mockk.coVerify {
                repository.getByDateRange(
                    "2026-02-16T00:00:00",
                    "2026-02-22T23:59:59",
                )
            }
        }
    }
}
