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
)

@Serializable data class HabitStatusSchema(val id: Long = 0, val habitId: Long, val date: Long)

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
