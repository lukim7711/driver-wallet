package com.driverwallet.app.feature.report.data

import android.content.Context
import androidx.core.content.FileProvider
import com.driverwallet.app.feature.report.domain.FileExporter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * Android implementation of [FileExporter].
 * Uses app cache dir + FileProvider for shareable URIs.
 */
class AndroidFileExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) : FileExporter {

    override suspend fun exportToFile(fileName: String, content: String): String {
        val cacheDir = File(context.cacheDir, "exports")
        cacheDir.mkdirs()
        val file = File(cacheDir, fileName)
        file.writeText(content)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return uri.toString()
    }
}
