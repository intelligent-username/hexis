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

package com.loc.hexis.core.data.backup.export

import com.loc.hexis.core.data.backup.ExportSchema
import com.loc.hexis.core.data.backup.HabitTimeDivisionPairSchema
import com.loc.hexis.core.data.backup.toCategorySchema
import com.loc.hexis.core.data.backup.toHabitSchema
import com.loc.hexis.core.data.backup.toHabitStatusSchema
import com.loc.hexis.core.data.backup.toPomodoroSessionSchema
import com.loc.hexis.core.data.backup.toTaskSchema
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.now
import com.loc.hexis.core.settings.backup.ExportRepo
import com.loc.hexis.core.tasks.PomodoroRepo
import com.loc.hexis.core.tasks.TaskRepo
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.writeString
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single(binds = [ExportRepo::class])
class ExportImpl(
    private val taskRepo: TaskRepo,
    private val habitsRepo: HabitRepo,
    private val pomodoroRepo: PomodoroRepo,
    private val settingsDatastore: SettingsDatastore,
) : ExportRepo {
    @OptIn(ExperimentalTime::class)
    override suspend fun exportToJson() {
        coroutineScope {
            val habitsDef =
                async {
                        withContext(Dispatchers.IO) {
                            habitsRepo.getHabits().map { it.toHabitSchema() }
                        }
                    }
                    .await()

            val statusesDef =
                async {
                        withContext(Dispatchers.IO) {
                            habitsRepo.getHabitStatuses().map { it.toHabitStatusSchema() }
                        }
                    }
                    .await()

            val tasksDef =
                async {
                        withContext(Dispatchers.IO) {
                            taskRepo.getTasks().map { it.toTaskSchema() }
                        }
                    }
                    .await()

            val categoriesDef =
                async {
                        withContext(Dispatchers.IO) {
                            taskRepo.getCategories().map { it.toCategorySchema() }
                        }
                    }
                    .await()

            val pomodoroSessionsDef =
                async {
                        withContext(Dispatchers.IO) {
                            pomodoroRepo.getAllSessions().map { it.toPomodoroSessionSchema() }
                        }
                    }
                    .await()

            val timeDivisionsDef =
                async {
                        withContext(Dispatchers.IO) { settingsDatastore.getTimeDivisions().first() }
                    }
                    .await()

            val pomodoroSettingsDef =
                async {
                        withContext(Dispatchers.IO) {
                            settingsDatastore.getPomodoroSettings().first()
                        }
                    }
                    .await()

            val habitTimeDivisionMapDef =
                async {
                        withContext(Dispatchers.IO) {
                            settingsDatastore.getHabitTimeDivisionMap().first().map {
                                HabitTimeDivisionPairSchema(it.key, it.value)
                            }
                        }
                    }
                    .await()

            val time = LocalDateTime.now().toString().replace(":", "").replace(" ", "")
            val file =
                FileKit.openFileSaver(
                    suggestedName = "Hexis-Export-$time",
                    defaultExtension = "json",
                )

            file?.writeString(
                Json.encodeToString(
                    ExportSchema(
                        habits = habitsDef,
                        habitStatus = statusesDef,
                        tasks = tasksDef,
                        categories = categoriesDef,
                        pomodoroSessions = pomodoroSessionsDef,
                        timeDivisions = timeDivisionsDef,
                        pomodoroSettings = pomodoroSettingsDef,
                        habitTimeDivisionPairs = habitTimeDivisionMapDef,
                    )
                )
            )
        }
    }
}
