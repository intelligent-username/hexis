package com.shub39.grit.core.settings.backup

interface ExportRepo {
    suspend fun exportToJson()
}

enum class ExportState {
    IDLE,
    EXPORTING,
    EXPORTED,
}