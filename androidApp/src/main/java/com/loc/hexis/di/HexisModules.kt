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

package com.loc.hexis.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.loc.hexis.core.data.datastore.DatastoreFactory
import com.loc.hexis.habits.data.database.HabitDatabase
import com.loc.hexis.habits.data.database.HabitDbFactory
import com.loc.hexis.habits.data.database.HabitStatusDao
import com.loc.hexis.habits.data.database.HabitsDao
import com.loc.hexis.note.data.database.NotesDao
import com.loc.hexis.shared.ui.di.UIModules
import com.loc.hexis.tasks.data.database.CategoryDao
import com.loc.hexis.tasks.data.database.PomodoroDao
import com.loc.hexis.tasks.data.database.TaskDatabase
import com.loc.hexis.tasks.data.database.TaskDbFactory
import com.loc.hexis.tasks.data.database.TasksDao
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [UIModules::class])
@ComponentScan("com.loc.hexis")
class HexisModules {
    @Single fun getHabitDb(dbFactory: HabitDbFactory): HabitDatabase = dbFactory.create().build()

    @Single fun getTaskDb(dbFactory: TaskDbFactory): TaskDatabase = dbFactory.create().build()

    @Single fun getHabitDao(db: HabitDatabase): HabitsDao = db.habitDao()

    @Single fun getTaskDao(db: TaskDatabase): TasksDao = db.taskDao()

    @Single fun getHabitStatusDao(db: HabitDatabase): HabitStatusDao = db.habitStatusDao()

    @Single fun getCategoryDao(db: TaskDatabase): CategoryDao = db.categoryDao()

    @Single fun getPomodoroDao(db: TaskDatabase): PomodoroDao = db.pomodoroDao()

    @Single fun getNotesDao(db: TaskDatabase): NotesDao = db.notesDao()

    @Single
    fun getDatastore(factory: DatastoreFactory): DataStore<Preferences> =
        factory.getPreferencesDataStore()
}
