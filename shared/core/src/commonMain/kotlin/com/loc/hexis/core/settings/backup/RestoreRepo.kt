
package com.loc.hexis.core.settings.backup

interface RestoreRepo {
    suspend fun restoreData(): RestoreResult
}

sealed class RestoreResult {
    data object Success : RestoreResult()

    data object Cancelled : RestoreResult()

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

    data object Conflict : RestoreFailedException

    data object PersistenceFailure : RestoreFailedException

    data object PartialRestore : RestoreFailedException
}

class SchemaMismatchException : Exception()
