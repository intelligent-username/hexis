package com.loc.hexis.habits.data

import com.loc.hexis.core.habits.DisplayMode
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.habits.HabitStatus
import com.loc.hexis.habits.data.database.HabitEntity
import com.loc.hexis.habits.data.database.HabitStatusEntity

private val LINE_BREAK_REGEX = Regex("\\r?\\n|\\r")

fun HabitEntity.toHabit(): Habit {
    return Habit(
        id = id,
        title = title.trim().replace(LINE_BREAK_REGEX, " "),
        description = description.trim().replace(LINE_BREAK_REGEX, " "),
        time = time,
        days = days,
        index = index,
        reminder = reminder,
        displayMode =
            try {
                DisplayMode.valueOf(displayMode)
            } catch (_: Exception) {
                DisplayMode.CHECKBOX
            },
        targetValue = targetValue ?: 1.0,
        pomodoroLinked = pomodoroLinked,
        incrementBy = incrementBy,
    )
}

fun HabitStatusEntity.toHabitStatus(): HabitStatus {
    return HabitStatus(id = id, habitId = habitId, date = date, value = value)
}

fun Habit.toHabitEntity(): HabitEntity {
    return HabitEntity(
        id = id,
        title = title.trim().replace(LINE_BREAK_REGEX, " "),
        description = description.trim().replace(LINE_BREAK_REGEX, " "),
        time = time,
        index = index,
        days = days,
        reminder = reminder,
        displayMode = displayMode.name,
        targetValue = targetValue,
        pomodoroLinked = pomodoroLinked,
        incrementBy = incrementBy,
    )
}

fun HabitStatus.toHabitStatusEntity(): HabitStatusEntity {
    return HabitStatusEntity(id = id, habitId = habitId, date = date, value = value)
}
