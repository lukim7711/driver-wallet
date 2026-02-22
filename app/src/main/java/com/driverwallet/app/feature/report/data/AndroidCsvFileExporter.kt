package com.driverwallet.app.feature.report.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.driverwallet.app.feature.report.domain.CsvFileExporter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class AndroidCsvFileExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) : CsvFileExporter {

    override fun writeCsv(fileName: String, content: String): Uri {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName).apply { writeText(content) }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }
}
