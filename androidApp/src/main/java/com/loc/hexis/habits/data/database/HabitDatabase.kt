package com.loc.hexis.habits.data.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.loc.hexis.core.data.Converters

@Database(
    entities = [HabitEntity::class, HabitStatusEntity::class],
    version = HabitDatabase.SCHEMA_VERSION,
    exportSchema = true,
)
@ColumnTypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitsDao

    abstract fun habitStatusDao(): HabitStatusDao

    companion object {
        const val SCHEMA_VERSION = 1
        const val DB_NAME = "habit_database"
    }
}