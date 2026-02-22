package com.driverwallet.app.feature.report.domain.usecase

import com.driverwallet.app.core.model.Categories
import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.feature.report.domain.CsvFileExporter
import com.driverwallet.app.shared.data.repository.TransactionRepository
import com.driverwallet.app.shared.domain.model.Transaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ExportCsvUseCaseTest {

    private val repository: TransactionRepository = mockk()
    private val csvFileExporter: CsvFileExporter = mockk()
    private lateinit var useCase: ExportCsvUseCase

    @BeforeEach
    fun setup() {
        every { csvFileExporter.writeCsv(any(), any()) } returns mockk()
        useCase = ExportCsvUseCase(repository, csvFileExporter)
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
            val contentSlot = slot<String>()
            every { csvFileExporter.writeCsv(any(), capture(contentSlot)) } returns mockk()

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            val lines = contentSlot.captured.lines().filter { it.isNotBlank() }
            assertEquals("Tanggal,Tipe,Kategori,Jumlah,Catatan", lines.first())
        }

        @Test
        fun `data rows formatted correctly`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns listOf(
                transaction(),
            )
            val contentSlot = slot<String>()
            every { csvFileExporter.writeCsv(any(), capture(contentSlot)) } returns mockk()

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            val lines = contentSlot.captured.lines().filter { it.isNotBlank() }
            assertEquals(2, lines.size)
            assertEquals("2026-02-20,Pengeluaran,BBM,50000,Pertamax", lines[1])
        }

        @Test
        fun `income type shows Pemasukan`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns listOf(
                transaction(type = TransactionType.INCOME, category = Categories.ORDER),
            )
            val contentSlot = slot<String>()
            every { csvFileExporter.writeCsv(any(), capture(contentSlot)) } returns mockk()

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            val lines = contentSlot.captured.lines().filter { it.isNotBlank() }
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
            val contentSlot = slot<String>()
            every { csvFileExporter.writeCsv(any(), capture(contentSlot)) } returns mockk()

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            val lines = contentSlot.captured.lines().filter { it.isNotBlank() }
            assertTrue(lines[1].endsWith("BBM; Pertamax 92"))
        }

        @Test
        fun `newline in note replaced with space`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns listOf(
                transaction(note = "Baris1\nBaris2"),
            )
            val contentSlot = slot<String>()
            every { csvFileExporter.writeCsv(any(), capture(contentSlot)) } returns mockk()

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            val dataLines = contentSlot.captured.lines().filter { it.isNotBlank() }
            assertEquals(2, dataLines.size)
        }
    }

    @Nested
    @DisplayName("Empty data")
    inner class EmptyData {

        @Test
        fun `empty transactions produces header only CSV`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns emptyList()
            val contentSlot = slot<String>()
            every { csvFileExporter.writeCsv(any(), capture(contentSlot)) } returns mockk()

            val result = useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            assertTrue(result.isSuccess)
            val lines = contentSlot.captured.lines().filter { it.isNotBlank() }
            assertEquals(1, lines.size)
        }
    }

    @Nested
    @DisplayName("Date range query")
    inner class DateRange {

        @Test
        fun `passes correct date range to repository`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns emptyList()

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            coVerify {
                repository.getByDateRange(
                    "2026-02-16T00:00:00",
                    "2026-02-22T23:59:59",
                )
            }
        }
    }

    @Nested
    @DisplayName("File name")
    inner class FileName {

        @Test
        fun `file name contains date range`() = runTest {
            coEvery { repository.getByDateRange(any(), any()) } returns emptyList()
            val fileNameSlot = slot<String>()
            every { csvFileExporter.writeCsv(capture(fileNameSlot), any()) } returns mockk()

            useCase(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22))

            assertEquals("laporan_2026-02-16_2026-02-22.csv", fileNameSlot.captured)
        }
    }
}
