/*
 * Copyright (C) 2025-2026 Hexis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
