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

package com.loc.hexis.core.data.backup.restore

import android.util.Log
import androidx.room.withTransaction
import com.loc.hexis.core.data.Converters
import com.loc.hexis.core.data.backup.ExportSchema
import com.loc.hexis.core.data.backup.toCategory
import com.loc.hexis.core.data.backup.toHabit
import com.loc.hexis.core.data.backup.toHabitStatus
import com.loc.hexis.core.data.backup.toNote
import com.loc.hexis.core.data.backup.toPomodoroSession
import com.loc.hexis.core.data.backup.toTask
import com.loc.hexis.core.habits.DisplayMode
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.habits.HabitStatus
import com.loc.hexis.core.interfaces.AlarmScheduler
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.note.Note
import com.loc.hexis.core.note.NoteRepo
import com.loc.hexis.core.note.NoteType
import com.loc.hexis.core.settings.backup.BackupSettings
import com.loc.hexis.core.settings.backup.BackupSettingsConflictException
import com.loc.hexis.core.settings.backup.BackupUserSettings
import com.loc.hexis.core.settings.backup.RestoreFailedException
import com.loc.hexis.core.settings.backup.RestoreRepo
import com.loc.hexis.core.settings.backup.RestoreResult
import com.loc.hexis.core.settings.backup.SchemaMismatchException
import com.loc.hexis.core.tasks.Category
import com.loc.hexis.core.tasks.PomodoroRepo
import com.loc.hexis.core.tasks.PomodoroSession
import com.loc.hexis.core.tasks.Task
import com.loc.hexis.core.tasks.TaskRepo
import com.loc.hexis.habits.data.database.HabitDatabase
import com.loc.hexis.habits.data.toHabit as toDomainHabit
import com.loc.hexis.habits.data.toHabitEntity
import com.loc.hexis.tasks.data.database.TaskDatabase
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [RestoreRepo::class])
class RestoreImpl(
    @Provided private val taskRepo: TaskRepo,
    @Provided private val habitRepo: HabitRepo,
    @Provided private val alarmScheduler: AlarmScheduler,
    @Provided private val pomodoroRepo: PomodoroRepo,
    @Provided private val noteRepo: NoteRepo,
    @Provided private val settingsDatastore: SettingsDatastore,
    @Provided private val habitDatabase: HabitDatabase,
    @Provided private val taskDatabase: TaskDatabase,
) : RestoreRepo {
    private val restoreMutex = Mutex()

    override suspend fun restoreData(): RestoreResult =
        restoreMutex.withLock {
            var phase = "picker"
            var recordsCommitted = false
            try {
                val file =
                    FileKit.openFilePicker(mode = FileKitMode.Single, type = FileKitType.File("json"))
                        ?: return@withLock RestoreResult.Cancelled
                withContext(Dispatchers.IO) {
                    phase = "read"
                    val contents = file.readString()
                    phase = "decode"
                    val schema =
                        try {
                            Json { ignoreUnknownKeys = true }.decodeFromString<ExportSchema>(contents)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            throw InvalidBackupException()
                        }
                    if (
                        schema.tasksSchemaVersion != TaskDatabase.SCHEMA_VERSION ||
                            schema.habitsSchemaVersion != HabitDatabase.SCHEMA_VERSION
                    ) {
                        throw SchemaMismatchException()
                    }

                    phase = "validation"
                    val records: Records
                    val settings: BackupSettings
                    try {
                        validateIds(schema.habits.map { it.id })
                        validateIds(schema.habitStatus.map { it.id })
                        validateIds(schema.categories.map { it.id })
                        validateIds(schema.tasks.map { it.id })
                        validateIds(schema.pomodoroSessions.map { it.id })
                        validateIds(schema.notes.map { it.id })
                        validateIds(schema.timeDivisions.map { it.id })
                        validateIds(schema.archivedHabitIds)
                        validateIds(schema.habitTimeDivisionPairs.map { it.habitId })
                        schema.habits.forEach {
                            DisplayMode.valueOf(it.displayMode)
                            if (it.days.isNotBlank()) {
                                it.days.split(",").forEach { day -> DayOfWeek.valueOf(day) }
                            }
                            require((it.targetValue ?: 1.0).isFinite() && (it.targetValue ?: 1.0) > 0)
                            require(it.incrementBy.isFinite() && it.incrementBy > 0)
                        }
                        schema.habitStatus.forEach { require(it.value.isFinite() && it.value >= 0) }
                        schema.tasks.forEach { require(it.categoryId > 0) }
                        schema.habitStatus.forEach { require(it.habitId > 0) }
                        schema.habitTimeDivisionPairs.forEach { require(it.divisionId > 0) }
                        schema.pomodoroSessions.forEach {
                            require(it.goalDurationMinutes > 0)
                            require(it.linkedHabitId == null || it.linkedHabitId > 0)
                            require(
                                it.timeCompletedMinutes == null ||
                                    (it.timeCompletedMinutes.isFinite() && it.timeCompletedMinutes >= 0)
                            )
                        }
                        schema.pomodoroSettings?.let {
                            require(it.focusMinutes.isFinite() && it.focusMinutes > 0)
                            require(it.shortBreakMinutes.isFinite() && it.shortBreakMinutes > 0)
                            require(it.longBreakMinutes.isFinite() && it.longBreakMinutes > 0)
                            require(it.longBreakInterval > 0)
                        }
                        schema.notes.forEach { NoteType.valueOf(it.type) }
                        schema.userSettings?.let {
                            DayOfWeek.valueOf(it.startOfTheWeek)
                            require(it.dayCutoffHour in 0..23)
                            require(it.vaultPasswordHash == null || it.vaultPasswordHash.isNotBlank())
                        }
                        require(
                            schema.notes.none { it.type == NoteType.VAULT.name } ||
                                !schema.userSettings?.vaultPasswordHash.isNullOrBlank()
                        )
                        records =
                            Records(
                                habits = schema.habits.map { canonical(it.toHabit()) },
                                statuses = schema.habitStatus.map { it.toHabitStatus() },
                                categories = schema.categories.map { it.toCategory() },
                                tasks = schema.tasks.map { canonical(it.toTask()) },
                                sessions = schema.pomodoroSessions.map { canonical(it.toPomodoroSession()) },
                                notes = schema.notes.map { canonical(it.toNote()) },
                                legacyTaskIds =
                                    schema.tasks.filter { it.description == null }.map { it.id }.toSet(),
                            )
                        settings =
                            BackupSettings(
                                timeDivisions = schema.timeDivisions,
                                habitTimeDivisionMap =
                                    schema.habitTimeDivisionPairs.associate { it.habitId to it.divisionId },
                                archivedHabitIds = schema.archivedHabitIds.toSet(),
                                pomodoroSettings = schema.pomodoroSettings,
                                userSettings =
                                    schema.userSettings?.let {
                                        BackupUserSettings(
                                            startOfTheWeek = DayOfWeek.valueOf(it.startOfTheWeek),
                                            is24Hr = it.is24Hr,
                                            dayCutoffEnabled = it.dayCutoffEnabled,
                                            dayCutoffHour = it.dayCutoffHour,
                                            compactHabitView = it.compactHabitView,
                                            taskReorderPref = it.taskReorderPref,
                                            habitReorderPref = it.habitReorderPref,
                                            putNewTasksAtTopPref = it.putNewTasksAtTopPref,
                                            showPomodoroPieChartPref = it.showPomodoroPieChartPref,
                                            lockVaultNotesPref = it.lockVaultNotesPref,
                                            vaultPasswordHash = it.vaultPasswordHash,
                                        )
                                    },
                            )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        throw InvalidBackupException()
                    }
                    pendingHabits(records)
                    pendingTasks(records, rechecking = false)
                    validateSettings(settings, records.habits, rechecking = false)

                    phase = "habit transaction"
                    val habitsChanged =
                        habitDatabase.withTransaction {
                            val (habits, statuses) = pendingHabits(records, rechecking = true)
                            habits.forEach { habitRepo.upsertHabit(it) }
                            statuses.forEach { habitRepo.insertHabitStatus(it) }
                            habits.isNotEmpty() || statuses.isNotEmpty()
                        }
                    recordsCommitted = habitsChanged

                    phase = "task transaction"
                    val tasksChanged =
                        taskDatabase.withTransaction {
                            val pending = pendingTasks(records, rechecking = true)
                            pending.categories.forEach { taskRepo.upsertCategory(it) }
                            pending.tasks.forEach { taskRepo.upsertTask(it) }
                            pending.sessions.forEach { pomodoroRepo.insertSession(it) }
                            pending.notes.forEach { noteRepo.upsertNote(it) }
                            pending.categories.isNotEmpty() ||
                                pending.tasks.isNotEmpty() ||
                                pending.sessions.isNotEmpty() ||
                                pending.notes.isNotEmpty()
                        }
                    recordsCommitted = recordsCommitted || tasksChanged

                    phase = "settings"
                    validateSettings(settings, records.habits, rechecking = true)
                    settingsDatastore.applyBackupSettings(settings)
                    phase = "settings committed"
                }
                phase = "reminder repair"
                try {
                    alarmScheduler.rescheduleAll()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("RestoreRepo", "Reminder repair failed: ${e.javaClass.simpleName}")
                }
                RestoreResult.Success
            } catch (e: CancellationException) {
                Log.w("RestoreRepo", "Cancelled during $phase; records committed: $recordsCommitted")
                throw e
            } catch (e: Exception) {
                Log.e("RestoreRepo", "Failed during $phase: ${e.javaClass.simpleName}")
                RestoreResult.Failure(
                    when {
                        recordsCommitted -> RestoreFailedException.PartialRestore
                        e is InvalidBackupException -> RestoreFailedException.InvalidFile
                        e is SchemaMismatchException -> RestoreFailedException.OldSchema
                        e is BackupSettingsConflictException -> RestoreFailedException.Conflict
                        else -> RestoreFailedException.PersistenceFailure
                    }
                )
            }
        }

    private suspend fun compatibleHabits(
        incoming: List<Habit>,
        requirePresent: Boolean = false,
    ): Map<Long, Habit> {
        val existing = habitRepo.getHabits().associate { it.id to canonical(it) }
        incoming.forEach { habit ->
            val old = existing[habit.id]
            checkCompatible((old == null && !requirePresent) || old == habit)
        }
        return existing + incoming.associateBy { it.id }
    }

    private suspend fun pendingHabits(
        records: Records,
        rechecking: Boolean = false,
    ): Pair<List<Habit>, List<HabitStatus>> {
        val compatible = compatibleHabits(records.habits)
        val existingIds = habitRepo.getHabits().map { it.id }.toSet()
        val existingStatuses = habitRepo.getHabitStatuses()
        val statusesById = existingStatuses.associateBy { it.id }
        val occupiedDates = existingStatuses.map { it.habitId to it.date }.toMutableSet()
        val statuses =
            records.statuses.filter { status ->
                checkReference(status.habitId in compatible, rechecking)
                val old = statusesById[status.id]
                checkCompatible(old == null || old == status)
                if (old == null) {
                    checkCompatible(occupiedDates.add(status.habitId to status.date))
                    true
                } else false
            }
        return records.habits.filter { it.id !in existingIds } to statuses
    }

    private suspend fun pendingTasks(records: Records, rechecking: Boolean): TaskChanges {
        val habits = compatibleHabits(records.habits, requirePresent = rechecking)
        val categoriesById = taskRepo.getCategories().associateBy { it.id }
        records.categories.forEach {
            checkCompatible(categoriesById[it.id]?.let { old -> old == it } != false)
        }
        val categoryIds = categoriesById.keys + records.categories.map { it.id }
        val tasksById = taskRepo.getTasks().associate { it.id to canonical(it) }
        val tasks =
            records.tasks.filter { task ->
                checkReference(task.categoryId in categoryIds, rechecking)
                val old = tasksById[task.id]
                val incoming =
                    if (old != null && task.id in records.legacyTaskIds) {
                        task.copy(description = old.description)
                    } else task
                checkCompatible(old == null || old == incoming)
                old == null
            }
        val sessionsById = pomodoroRepo.getAllSessions().associate { it.id to canonical(it) }
        val sessions =
            records.sessions.filter { session ->
                checkReference(session.linkedHabitId == null || session.linkedHabitId in habits, rechecking)
                val old = sessionsById[session.id]
                checkCompatible(old == null || old == session)
                old == null
            }
        val notes =
            records.notes.filter { note ->
                val old = noteRepo.getNoteById(note.id)?.let { canonical(it) }
                checkCompatible(old == null || old == note)
                old == null
            }
        return TaskChanges(
            categories = records.categories.filter { it.id !in categoriesById },
            tasks = tasks,
            sessions = sessions,
            notes = notes,
        )
    }

    private suspend fun validateSettings(
        settings: BackupSettings,
        incomingHabits: List<Habit>,
        rechecking: Boolean,
    ) {
        val habits = compatibleHabits(incomingHabits, requirePresent = rechecking)
        val merged =
            settings.mergedWith(
                currentDivisions = settingsDatastore.getTimeDivisions().first(),
                currentAssignments = settingsDatastore.getHabitTimeDivisionMap().first(),
                currentArchives = settingsDatastore.getArchivedHabitIds().first(),
                currentVaultHash = settingsDatastore.getVaultPasswordHash().first(),
            )
        checkReference(settings.archivedHabitIds.all { it in habits }, rechecking)
        checkReference(settings.habitTimeDivisionMap.keys.all { it in habits }, rechecking)
        checkReference(
            settings.habitTimeDivisionMap.values.all { id -> merged.timeDivisions.any { it.id == id } },
            rechecking,
        )
    }

    private fun validateIds(ids: List<Long>) {
        require(ids.all { it > 0 } && ids.size == ids.toSet().size)
    }

    private fun checkCompatible(compatible: Boolean) {
        if (!compatible) throw BackupSettingsConflictException()
    }

    private fun checkReference(valid: Boolean, rechecking: Boolean) {
        if (!valid) {
            if (rechecking) throw BackupSettingsConflictException()
            throw InvalidBackupException()
        }
    }

    private fun persistedTime(time: LocalDateTime?): LocalDateTime? =
        Converters.dateFromTimestamp(Converters.dateToTimestamp(time))

    private fun canonical(habit: Habit): Habit =
        habit.toHabitEntity().toDomainHabit().copy(time = persistedTime(habit.time)!!)

    private fun canonical(task: Task): Task = task.copy(reminder = persistedTime(task.reminder))

    private fun canonical(session: PomodoroSession): PomodoroSession =
        session.copy(
            goalDurationMinutes = session.goalDurationMinutes.toInt().toFloat(),
            timeStarted = persistedTime(session.timeStarted)!!,
            timeFinished = persistedTime(session.timeFinished),
        )

    private fun canonical(note: Note): Note =
        note.copy(createdAt = persistedTime(note.createdAt)!!, updatedAt = persistedTime(note.updatedAt)!!)

    private data class Records(
        val habits: List<Habit>,
        val statuses: List<HabitStatus>,
        val categories: List<Category>,
        val tasks: List<Task>,
        val sessions: List<PomodoroSession>,
        val notes: List<Note>,
        val legacyTaskIds: Set<Long>,
    )

    private data class TaskChanges(
        val categories: List<Category>,
        val tasks: List<Task>,
        val sessions: List<PomodoroSession>,
        val notes: List<Note>,
    )

    private class InvalidBackupException : Exception()
}
