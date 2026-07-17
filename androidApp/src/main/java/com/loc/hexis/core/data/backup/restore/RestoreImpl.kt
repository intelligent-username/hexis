package com.loc.hexis.core.data.backup.restore

import android.util.Log
import com.loc.hexis.core.data.backup.ExportSchema
import com.loc.hexis.core.data.backup.toCategory
import com.loc.hexis.core.data.backup.toHabit
import com.loc.hexis.core.data.backup.toHabitStatus
import com.loc.hexis.core.data.backup.toPomodoroSession
import com.loc.hexis.core.data.backup.toTask
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.interfaces.AlarmScheduler
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.settings.backup.RestoreFailedException
import com.loc.hexis.core.settings.backup.RestoreRepo
import com.loc.hexis.core.settings.backup.RestoreResult
import com.loc.hexis.core.settings.backup.SchemaMismatchException
import com.loc.hexis.core.tasks.PomodoroRepo
import com.loc.hexis.core.tasks.TaskRepo
import com.loc.hexis.habits.data.database.HabitDatabase
import com.loc.hexis.tasks.data.database.TaskDatabase
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single(binds = [RestoreRepo::class])
class RestoreImpl(
    private val taskRepo: TaskRepo,
    private val habitRepo: HabitRepo,
    private val alarmScheduler: AlarmScheduler,
    private val pomodoroRepo: PomodoroRepo,
    private val settingsDatastore: SettingsDatastore,
) : RestoreRepo {
    override suspend fun restoreData(): RestoreResult {
        return try {
            val file =
                FileKit.openFilePicker(mode = FileKitMode.Single, type = FileKitType.File("json"))

            if (file == null) {
                return RestoreResult.Failure(exceptionType = RestoreFailedException.InvalidFile)
            }

            val json = Json { ignoreUnknownKeys = true }

            val jsonDeserialized = json.decodeFromString<ExportSchema>(file.readString())

            if (
                jsonDeserialized.tasksSchemaVersion != TaskDatabase.SCHEMA_VERSION ||
                    jsonDeserialized.habitsSchemaVersion != HabitDatabase.SCHEMA_VERSION
            ) {
                throw SchemaMismatchException()
            }

            withContext(Dispatchers.IO) {
                awaitAll(
                    async {
                        habitRepo.getHabits().forEach { alarmScheduler.cancel(it) }
                        alarmScheduler.cancelAll()

                        jsonDeserialized.habits
                            .map { it.toHabit() }
                            .forEach {
                                habitRepo.upsertHabit(it)
                                alarmScheduler.schedule(it)
                            }

                        jsonDeserialized.habitStatus
                            .map { it.toHabitStatus() }
                            .forEach { habitRepo.insertHabitStatus(it) }
                    },
                    async {
                        jsonDeserialized.categories
                            .map { it.toCategory() }
                            .forEach { taskRepo.upsertCategory(it) }

                        jsonDeserialized.tasks
                            .map { it.toTask() }
                            .forEach { taskRepo.upsertTask(it) }
                    },
                    async {
                        jsonDeserialized.pomodoroSessions
                            .map { it.toPomodoroSession() }
                            .forEach { pomodoroRepo.insertSession(it) }

                        settingsDatastore.setTimeDivisions(jsonDeserialized.timeDivisions)

                        jsonDeserialized.pomodoroSettings?.let {
                            settingsDatastore.setPomodoroSettings(it)
                        }

                        settingsDatastore.setHabitTimeDivisionMap(
                            jsonDeserialized.habitTimeDivisionPairs.associate {
                                it.habitId to it.divisionId
                            }
                        )
                    },
                )
            }

            RestoreResult.Success
        } catch (e: SchemaMismatchException) {
            Log.e("RestoreRepo", "Failed to restore data, old schema: ", e)
            RestoreResult.Failure(RestoreFailedException.OldSchema)
        } catch (e: SerializationException) {
            Log.e("RestoreRepo", "Failed to deserialize, invalid file: ", e)
            RestoreResult.Failure(RestoreFailedException.InvalidFile)
        }
    }
}
