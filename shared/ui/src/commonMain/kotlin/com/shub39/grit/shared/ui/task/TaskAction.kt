package com.shub39.grit.shared.ui.task

import com.shub39.grit.core.tasks.Category
import com.shub39.grit.core.tasks.Task

sealed interface TaskAction {
    data class AddCategory(val category: Category) : TaskAction

    data class ChangeCategory(val category: Category) : TaskAction

    data class DeleteCategory(val category: Category) : TaskAction

    data object DeleteTasks : TaskAction

    data class DeleteTask(val task: Task) : TaskAction

    data class ReorderTasks(val mapping: List<Pair<Int, Task>>) : TaskAction

    data class ReorderCategories(val mapping: List<Pair<Int, Category>>) : TaskAction

    data class UpsertTask(val task: Task) : TaskAction

    data class ToggleAddTaskSheet(val show: Boolean) : TaskAction
}