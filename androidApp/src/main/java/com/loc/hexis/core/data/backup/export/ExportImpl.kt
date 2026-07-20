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
