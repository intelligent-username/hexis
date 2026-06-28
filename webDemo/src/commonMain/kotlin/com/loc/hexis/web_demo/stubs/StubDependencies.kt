package com.loc.hexis.web_demo.stubs

import com.loc.hexis.core.app.Changelog
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.interfaces.AlarmScheduler
import com.loc.hexis.core.interfaces.BiometricUtils
import com.loc.hexis.core.interfaces.ChangelogManager
import com.loc.hexis.core.settings.backup.ExportRepo
import com.loc.hexis.core.settings.backup.RestoreRepo
import com.loc.hexis.core.settings.backup.RestoreResult
import com.loc.hexis.core.tasks.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Single

@Single(binds = [ChangelogManager::class])
class ChangelogManagerStub : ChangelogManager {
    override val changelogs: Flow<Changelog> = flowOf(emptyList())
}

@Single(binds = [AlarmScheduler::class])
class AlarmSchedulerStub : AlarmScheduler {
    override fun schedule(habit: Habit) {}

    override fun schedule(task: Task) {}

    override fun cancel(habit: Habit) {}

    override fun cancel(task: Task) {}

    override fun cancelAll() {}
}

@Single(binds = [ExportRepo::class])
class ExportRepoStub : ExportRepo {
    override suspend fun exportToJson() {}
}

@Single(binds = [RestoreRepo::class])
class RestoreRepoStub : RestoreRepo {
    override suspend fun restoreData(): RestoreResult = RestoreResult.Success
}

@Single(binds = [BiometricUtils::class])
class BiometricUtilsStub : BiometricUtils {
    override fun getAuthenticators(): Int = 0

    override fun authenticationAvailable(): Boolean = false
}