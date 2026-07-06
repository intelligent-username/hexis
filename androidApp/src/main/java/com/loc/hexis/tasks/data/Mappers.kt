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

package com.loc.hexis.tasks.data

import com.loc.hexis.core.tasks.Category
import com.loc.hexis.core.tasks.PomodoroSession
import com.loc.hexis.core.tasks.Task
import com.loc.hexis.tasks.data.database.CategoryEntity
import com.loc.hexis.tasks.data.database.PomodoroSessionEntity
import com.loc.hexis.tasks.data.database.TaskEntity

fun Task.toTaskEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        categoryId = categoryId,
        title = title,
        index = index,
        status = status,
        reminder = reminder,
    )
}

fun TaskEntity.toTask(): Task {
    return Task(
        id = id,
        categoryId = categoryId,
        title = title,
        index = index,
        status = status,
        reminder = reminder,
    )
}

fun CategoryEntity.toCategory(): Category {
    return Category(id = id, name = name, index = index, color = color)
}

fun Category.toCategoryEntity(): CategoryEntity {
    return CategoryEntity(id = id, name = name, color = color, index = index)
}

fun PomodoroSessionEntity.toPomodoroSession(): PomodoroSession {
    return PomodoroSession(
        id = id,
        goalDurationMinutes = goalDurationMinutes,
        timeStarted = timeStarted,
        timeFinished = timeFinished,
        completed = completed,
        timeCompletedMinutes = timeCompletedMinutes,
        linkedHabitId = linkedHabitId,
    )
}

fun PomodoroSession.toPomodoroSessionEntity(): PomodoroSessionEntity {
    return PomodoroSessionEntity(
        id = id,
        goalDurationMinutes = goalDurationMinutes,
        timeStarted = timeStarted,
        timeFinished = timeFinished,
        completed = completed,
        timeCompletedMinutes = timeCompletedMinutes,
        linkedHabitId = linkedHabitId,
    )
}
