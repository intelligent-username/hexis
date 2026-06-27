package com.shub39.grit.tasks.data.database

import androidx.room3.AutoMigration
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.shub39.grit.core.data.Converters

@Database(
    entities = [TaskEntity::class, CategoryEntity::class, PomodoroSessionEntity::class],
    version = TaskDatabase.SCHEMA_VERSION,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 4, to = 5), AutoMigration(from = 5, to = 6)],
)
@ColumnTypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TasksDao

    abstract fun categoryDao(): CategoryDao

    abstract fun pomodoroDao(): PomodoroDao

    companion object {
        const val DB_NAME = "task_database"
        const val SCHEMA_VERSION = 6
    }
}
