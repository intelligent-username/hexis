package com.loc.hexis.tasks.data.database

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {
    @Query("SELECT * FROM task") fun getTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task") suspend fun getTasks(): List<TaskEntity>

    @Query("UPDATE task SET `index` = :newIndex WHERE id = :id")
    suspend fun updateTaskIndexById(id: Long, newIndex: Int)

    @Query("SELECT * FROM task WHERE id = :id") suspend fun getTaskById(id: Long): TaskEntity?

    @Upsert suspend fun upsertTask(taskEntity: TaskEntity)

    @Delete suspend fun deleteTask(taskEntity: TaskEntity)

    @Query("DELETE FROM task") suspend fun deleteAllTasks()
}
