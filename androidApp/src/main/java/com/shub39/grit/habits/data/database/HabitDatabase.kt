package com.shub39.grit.habits.data.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.shub39.grit.core.data.Converters

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
        const val SCHEMA_VERSION = 6
        const val DB_NAME = "habit_database"

        val migrate_3_4 =
            object : Migration(3, 4) {
                override suspend fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "ALTER TABLE habit_index ADD COLUMN days TEXT NOT NULL DEFAULT '${Converters.allDays}'"
                    )
                }
            }

        val migrate_5_6 =
            object : Migration(5, 6) {
                override suspend fun migrate(connection: SQLiteConnection) {
                    connection.execSQL("ALTER TABLE habit_index ADD COLUMN displayMode TEXT NOT NULL DEFAULT 'CHECKBOX'")
                    connection.execSQL("ALTER TABLE habit_index ADD COLUMN targetValue REAL DEFAULT 1.0")
                    connection.execSQL("ALTER TABLE habit_index ADD COLUMN pomodoroLinked INTEGER NOT NULL DEFAULT 0")
                    connection.execSQL("ALTER TABLE habit_index ADD COLUMN incrementBy REAL NOT NULL DEFAULT 1.0")
                    connection.execSQL("ALTER TABLE habit_status ADD COLUMN value REAL NOT NULL DEFAULT 1.0")
                }
            }
    }
}