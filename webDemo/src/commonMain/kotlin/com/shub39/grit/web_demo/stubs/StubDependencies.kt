package com.shub39.grit.web_demo.stubs

import com.shub39.grit.core.app.Changelog
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.interfaces.AlarmScheduler
import com.shub39.grit.core.interfaces.BiometricUtils
import com.shub39.grit.core.interfaces.ChangelogManager
import com.shub39.grit.core.settings.backup.ExportRepo
import com.shub39.grit.core.settings.backup.RestoreRepo
import com.shub39.grit.core.settings.backup.RestoreResult
import com.shub39.grit.core.tasks.Task
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