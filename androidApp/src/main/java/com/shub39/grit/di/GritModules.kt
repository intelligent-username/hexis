package com.shub39.grit.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.shub39.grit.core.data.datastore.DatastoreFactory
import com.shub39.grit.habits.data.database.HabitDatabase
import com.shub39.grit.habits.data.database.HabitDbFactory
import com.shub39.grit.habits.data.database.HabitStatusDao
import com.shub39.grit.habits.data.database.HabitsDao
import com.shub39.grit.shared.ui.di.UIModules
import com.shub39.grit.tasks.data.database.CategoryDao
import com.shub39.grit.tasks.data.database.PomodoroDao
import com.shub39.grit.tasks.data.database.TaskDatabase
import com.shub39.grit.tasks.data.database.TaskDbFactory
import com.shub39.grit.tasks.data.database.TasksDao
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [UIModules::class])
@ComponentScan("com.shub39.grit")
class HexisModules {
    @Single fun getHabitDb(dbFactory: HabitDbFactory): HabitDatabase = dbFactory.create().build()

    @Single fun getTaskDb(dbFactory: TaskDbFactory): TaskDatabase = dbFactory.create().build()

    @Single fun getHabitDao(db: HabitDatabase): HabitsDao = db.habitDao()

    @Single fun getTaskDao(db: TaskDatabase): TasksDao = db.taskDao()

    @Single fun getHabitStatusDao(db: HabitDatabase): HabitStatusDao = db.habitStatusDao()

    @Single fun getCategoryDao(db: TaskDatabase): CategoryDao = db.categoryDao()

    @Single fun getPomodoroDao(db: TaskDatabase): PomodoroDao = db.pomodoroDao()

    @Single
    fun getDatastore(factory: DatastoreFactory): DataStore<Preferences> =
        factory.getPreferencesDataStore()
}