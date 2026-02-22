package com.driverwallet.app.feature.report.domain

import android.net.Uri

interface CsvFileExporter {
    fun writeCsv(fileName: String, content: String): Uri
}
