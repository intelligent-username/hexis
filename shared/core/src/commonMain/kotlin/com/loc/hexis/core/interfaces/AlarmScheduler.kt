package com.loc.hexis.core.interfaces

import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.tasks.Task

interface AlarmScheduler {
    fun schedule(habit: Habit)

    fun schedule(task: Task)

    fun cancel(habit: Habit)

    fun cancel(task: Task)

    fun cancelAll()
}
