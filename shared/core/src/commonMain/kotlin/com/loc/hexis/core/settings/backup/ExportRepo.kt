package com.loc.hexis.core.settings.backup

interface ExportRepo {
    suspend fun exportToJson()
}

enum class ExportState {
    IDLE,
    EXPORTING,
    EXPORTED,
}
