package com.loc.hexis.core.data.backup

import com.loc.hexis.core.data.Converters
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.habits.HabitStatus
import com.loc.hexis.core.tasks.Category
import com.loc.hexis.core.tasks.PomodoroSession
import com.loc.hexis.core.tasks.Task
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun Habit.toHabitSchema(): HabitSchema {
    return HabitSchema(
        id = id,
        title = title,
        description = description,
        index = index,
        time = time.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
        days = Converters.dayOfWeekToString(days),
        reminder = reminder,
    )
}

fun HabitSchema.toHabit(): Habit {
    return Habit(
        id = id,
        title = title,
        description = description,
        index = index,
        time = Instant.fromEpochMilliseconds(time).toLocalDateTime(TimeZone.currentSystemDefault()),
        days = Converters.dayOfWeekFromString(days),
        reminder = reminder,
    )
}

fun HabitStatus.toHabitStatusSchema(): HabitStatusSchema {
    return HabitStatusSchema(id = id, habitId = habitId, date = Converters.dayToTimestamp(date))
}

fun HabitStatusSchema.toHabitStatus(): HabitStatus {
    return HabitStatus(id = id, habitId = habitId, date = Converters.dayFromTimestamp(date))
}

fun TaskSchema.toTask(): Task {
    return Task(
        id = id,
        categoryId = categoryId,
        title = title,
        status = status,
        index = index,
        reminder = reminder?.let { Converters.dateFromTimestamp(it) },
    )
}

fun Task.toTaskSchema(): TaskSchema {
    return TaskSchema(
        id = id,
        categoryId = categoryId,
        title = title,
        status = status,
        index = index,
        reminder = reminder?.let { Converters.dateToTimestamp(it) },
    )
}

fun CategorySchema.toCategory(): Category {
    return Category(id = id, name = name, index = index, color = color)
}

fun Category.toCategorySchema(): CategorySchema {
    return CategorySchema(id = id, name = name, index = index, color = color)
}

fun PomodoroSession.toPomodoroSessionSchema(): PomodoroSessionSchema {
    return PomodoroSessionSchema(
        id = id,
        goalDurationMinutes = goalDurationMinutes,
        timeStarted = Converters.dateToTimestamp(timeStarted)!!,
        timeFinished = Converters.dateToTimestamp(timeFinished),
        completed = completed,
        timeCompletedMinutes = timeCompletedMinutes,
        linkedHabitId = linkedHabitId,
    )
}

fun PomodoroSessionSchema.toPomodoroSession(): PomodoroSession {
    return PomodoroSession(
        id = id,
        goalDurationMinutes = goalDurationMinutes,
        timeStarted = Converters.dateFromTimestamp(timeStarted)!!,
        timeFinished = Converters.dateFromTimestamp(timeFinished),
        completed = completed,
        timeCompletedMinutes = timeCompletedMinutes,
        linkedHabitId = linkedHabitId,
    )
}
