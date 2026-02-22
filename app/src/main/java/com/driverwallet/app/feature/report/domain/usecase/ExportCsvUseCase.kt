package com.driverwallet.app.feature.report.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.driverwallet.app.core.model.TransactionType
import com.driverwallet.app.shared.data.repository.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExportCsvUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): Result<Uri> {
        return runCatching {
            val startStr = "${startDate}T00:00:00"
            val endStr = "${endDate}T23:59:59"

            val transactions = transactionRepository.getByDateRange(startStr, endStr)

            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val fileName = "laporan_${startDate.format(dateFormatter)}_${endDate.format(dateFormatter)}.csv"

            val csvContent = buildString {
                appendLine("Tanggal,Tipe,Kategori,Jumlah,Catatan")
                transactions.forEach { txn ->
                    val date = txn.createdAt.substring(0, 10)
                    val type = if (txn.type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran"
                    val category = sanitize(txn.category?.label ?: "Lainnya")
                    val amount = txn.amount.toString()
                    val note = sanitize(txn.note)
                    appendLine("$date,$type,$category,$amount,$note")
                }
            }

            val cacheDir = File(context.cacheDir, "exports")
            cacheDir.mkdirs()
            val file = File(cacheDir, fileName)
            file.writeText(csvContent)

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
        }
    }

    private fun sanitize(value: String): String =
        value.replace(",", ";").replace("\n", " ").replace("\r", "")
}
