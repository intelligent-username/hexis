
package com.loc.hexis.habits.data.database

import androidx.room.TypeConverters
import androidx.room.Database
import androidx.room.RoomDatabase
import com.loc.hexis.core.data.Converters

@Database(
    entities = [HabitEntity::class, HabitStatusEntity::class],
    version = HabitDatabase.SCHEMA_VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitsDao

    abstract fun habitStatusDao(): HabitStatusDao

    companion object {
        const val SCHEMA_VERSION = 1
        const val DB_NAME = "habit_database"
    }
}
