
package com.loc.hexis.core.settings.backup

interface ExportRepo {
    suspend fun exportToJson(): ExportResult
}

enum class ExportResult {
    SUCCESS,
    CANCELLED,
    FAILURE,
}

enum class ExportState {
    IDLE,
    EXPORTING,
    EXPORTED,
}
