/*
 * Copyright (C) 2025-2026 Hexis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.loc.hexis.tasks.data.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.loc.hexis.core.data.Converters
import com.loc.hexis.note.data.database.NoteEntity
import com.loc.hexis.note.data.database.NotesDao

@Database(
    entities =
        [TaskEntity::class, CategoryEntity::class, PomodoroSessionEntity::class, NoteEntity::class],
    version = TaskDatabase.SCHEMA_VERSION,
    exportSchema = true,
)
@ColumnTypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TasksDao

    abstract fun categoryDao(): CategoryDao

    abstract fun pomodoroDao(): PomodoroDao

    abstract fun notesDao(): NotesDao

    companion object {
        const val DB_NAME = "task_database"
        const val SCHEMA_VERSION = 3
    }
}
