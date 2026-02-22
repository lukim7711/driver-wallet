package com.driverwallet.app.feature.report.domain

/**
 * Domain-layer abstraction for file export operations.
 * Implementation details (Context, FileProvider, etc.) live in data layer.
 */
interface FileExporter {
    /**
     * Write [content] to a temporary file with the given [fileName]
     * and return a shareable URI string.
     */
    suspend fun exportToFile(fileName: String, content: String): String
}
