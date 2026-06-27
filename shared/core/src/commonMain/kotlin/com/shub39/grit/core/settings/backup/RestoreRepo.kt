package com.shub39.grit.core.settings.backup

interface RestoreRepo {
    suspend fun restoreData(): RestoreResult
}

sealed class RestoreResult {
    data object Success : RestoreResult()

    data class Failure(val exceptionType: RestoreFailedException) : RestoreResult()
}

enum class RestoreState {
    IDLE,
    RESTORING,
    RESTORED,
    FAILURE,
}

sealed interface RestoreFailedException {
    data object InvalidFile : RestoreFailedException

    data object OldSchema : RestoreFailedException
}

class SchemaMismatchException : Exception()