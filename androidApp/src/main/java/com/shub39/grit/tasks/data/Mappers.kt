package com.shub39.grit.tasks.data

import com.shub39.grit.core.tasks.Category
import com.shub39.grit.core.tasks.PomodoroSession
import com.shub39.grit.core.tasks.Task
import com.shub39.grit.tasks.data.database.CategoryEntity
import com.shub39.grit.tasks.data.database.PomodoroSessionEntity
import com.shub39.grit.tasks.data.database.TaskEntity

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
    )
}