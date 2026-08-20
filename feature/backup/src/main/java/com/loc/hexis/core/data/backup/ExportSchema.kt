package com.loc.hexis.core.data.backup

import com.loc.hexis.core.habits.TimeDivision
import com.loc.hexis.core.tasks.PomodoroSettings
import com.loc.hexis.habits.data.database.HabitDatabase
import com.loc.hexis.tasks.data.database.TaskDatabase
import kotlinx.serialization.Serializable

@Serializable
data class ExportSchema(
    val tasksSchemaVersion: Int = TaskDatabase.SCHEMA_VERSION,
    val habitsSchemaVersion: Int = HabitDatabase.SCHEMA_VERSION,
    val habits: List<HabitSchema>,
    val habitStatus: List<HabitStatusSchema>,
    val tasks: List<TaskSchema>,
    val categories: List<CategorySchema>,
    val pomodoroSessions: List<PomodoroSessionSchema> = emptyList(),
    val timeDivisions: List<TimeDivision> = emptyList(),
    val pomodoroSettings: PomodoroSettings? = null,
    val habitTimeDivisionPairs: List<HabitTimeDivisionPairSchema> = emptyList(),
    val notes: List<NoteSchema> = emptyList(),
    val archivedHabitIds: List<Long> = emptyList(),
    val userSettings: UserSettingsSchema? = null,
)

@Serializable
data class HabitSchema(
    val id: Long = 0,
    val title: String,
    val description: String,
    val index: Int,
    val time: Long,
    val days: String,
    val reminder: Boolean,
    val displayMode: String = "CHECKBOX",
    val targetValue: Double? = 1.0,
    val pomodoroLinked: Boolean = false,
    val incrementBy: Double = 1.0,
)

@Serializable
data class HabitStatusSchema(
    val id: Long = 0,
    val habitId: Long,
    val date: Long,
    val value: Double = 1.0,
)

@Serializable
data class TaskSchema(
    val id: Long = 0,
    val categoryId: Long,
    val title: String,
    val status: Boolean = false,
    val index: Int = 0,
    val reminder: Long? = null,
)

@Serializable
data class CategorySchema(val id: Long = 0, val name: String, val index: Int = 0, val color: String)

@Serializable
data class PomodoroSessionSchema(
    val id: Long = 0,
    val goalDurationMinutes: Int,
    val timeStarted: Long,
    val timeFinished: Long? = null,
    val completed: Boolean = false,
    val timeCompletedMinutes: Float? = null,
    val linkedHabitId: Long? = null,
)

@Serializable data class HabitTimeDivisionPairSchema(val habitId: Long, val divisionId: Long)

@Serializable
data class NoteSchema(
    val id: Long = 0,
    val title: String,
    val content: String = "",
    val type: String = "MARKDOWN",
    val payloadJson: String? = null,
    val metadata: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
    val pinned: Boolean = false,
    val archived: Boolean = false,
)

@Serializable
data class UserSettingsSchema(
    val startOfTheWeek: String = "MONDAY",
    val is24Hr: Boolean = false,
    val dayCutoffEnabled: Boolean = false,
    val dayCutoffHour: Int = 4,
    val compactHabitView: Boolean = false,
    val taskReorderPref: Boolean = true,
    val habitReorderPref: Boolean = false,
    val putNewTasksAtTopPref: Boolean = false,
    val showPomodoroPieChartPref: Boolean = true,
    val lockVaultNotesPref: Boolean = false,
    val vaultPasswordHash: String? = null,
)

