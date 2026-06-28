package com.loc.hexis.tasks.data.repository

import com.loc.hexis.core.data.notification.HexisNotificationManager
import com.loc.hexis.core.tasks.Category
import com.loc.hexis.core.tasks.Task
import com.loc.hexis.core.tasks.TaskRepo
import com.loc.hexis.tasks.data.database.CategoryDao
import com.loc.hexis.tasks.data.database.TasksDao
import com.loc.hexis.tasks.data.toCategory
import com.loc.hexis.tasks.data.toCategoryEntity
import com.loc.hexis.tasks.data.toTask
import com.loc.hexis.tasks.data.toTaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [TaskRepo::class])
class TasksRepository(
    private val tasksDao: TasksDao,
    private val categoryDao: CategoryDao,
    private val notificationManager: HexisNotificationManager,
) : TaskRepo {

    private val tasksFlow =
        tasksDao
            .getTasksFlow()
            .map { entities -> entities.map { it.toTask() }.sortedBy { it.index } }
            .flowOn(Dispatchers.IO)

    val categoriesFlow =
        categoryDao
            .getCategoriesFlow()
            .map { entities -> entities.map { it.toCategory() }.sortedBy { it.index } }
            .flowOn(Dispatchers.IO)

    override fun getTasksFlow(): Flow<Map<Category, List<Task>>> {
        return tasksFlow
            .combine(categoriesFlow) { tasks, categories ->
                categories.associateWith { category ->
                    tasks.filter { it.categoryId == category.id }
                }
            }
            .flowOn(Dispatchers.Default)
    }

    override fun getCompletedTasksFlow(): Flow<List<Task>> {
        return tasksFlow.map { tasks -> tasks.filter { it.status } }.flowOn(Dispatchers.IO)
    }

    override suspend fun getTasks(): List<Task> {
        return tasksDao.getTasks().map { it.toTask() }
    }

    override suspend fun getTaskById(id: Long): Task? {
        return tasksDao.getTaskById(id)?.toTask()
    }

    override suspend fun getCategories(): List<Category> {
        return categoryDao.getCategories().map { it.toCategory() }
    }

    override suspend fun updateTaskIndexById(id: Long, index: Int) {
        tasksDao.updateTaskIndexById(id, index)
    }

    override suspend fun upsertTask(task: Task) {
        tasksDao.upsertTask(task.toTaskEntity())

        if (task.status) {
            notificationManager.cancelNotification(task)
        }
    }

    override suspend fun deleteTask(task: Task) {
        tasksDao.deleteTask(task.toTaskEntity())
    }

    override suspend fun deleteAllTasks() {
        tasksDao.deleteAllTasks()
    }

    override suspend fun upsertCategory(category: Category) {
        categoryDao.upsertCategory(category.toCategoryEntity())
    }

    override suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category.toCategoryEntity())
    }

    override suspend fun deleteAllCategories() {
        categoryDao.deleteAllCategories()
    }
}