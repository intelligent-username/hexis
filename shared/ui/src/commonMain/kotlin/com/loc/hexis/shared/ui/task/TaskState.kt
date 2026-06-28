package com.loc.hexis.shared.ui.task

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.loc.hexis.core.tasks.Category
import com.loc.hexis.core.tasks.Task

@Stable
@Immutable
data class TaskState(
    val tasks: Map<Category, List<Task>> = emptyMap(),
    val currentCategory: Category? = null,
    val completedTasks: List<Task> = emptyList(),
    val is24Hour: Boolean = false,
    val reorderTasks: Boolean = true,
    val showAddTaskSheet: Boolean = false,
)