package com.loc.hexis.tasks.data.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.loc.hexis.core.data.Converters

@Database(
    entities = [TaskEntity::class, CategoryEntity::class, PomodoroSessionEntity::class],
    version = TaskDatabase.SCHEMA_VERSION,
    exportSchema = true,
)
@ColumnTypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TasksDao

    abstract fun categoryDao(): CategoryDao

    abstract fun pomodoroDao(): PomodoroDao

    companion object {
        const val DB_NAME = "task_database"
        const val SCHEMA_VERSION = 1
    }
}
