package com.shub39.grit.core.interfaces

import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.tasks.Task

interface AlarmScheduler {
    fun schedule(habit: Habit)

    fun schedule(task: Task)

    fun cancel(habit: Habit)

    fun cancel(task: Task)

    fun cancelAll()
}