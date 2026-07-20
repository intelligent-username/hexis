package com.loc.hexis.tasks.data.database

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.execSQL
import org.koin.core.annotation.Single

@Single
class TaskDbFactory(private val context: Context) {
    fun create(): RoomDatabase.Builder<TaskDatabase> {
        val appContext = context.applicationContext
        val dbfile = appContext.getDatabasePath(TaskDatabase.DB_NAME)

        return Room.databaseBuilder<TaskDatabase>(appContext, dbfile.absolutePath)
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
    }

    companion object {
        private val MIGRATION_2_3 =
            Migration(2, 3) { db ->
                db.execSQL("ALTER TABLE notes ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
            }

        private val MIGRATION_3_4 =
            Migration(3, 4) { db ->
                db.execSQL("ALTER TABLE task ADD COLUMN description TEXT NOT NULL DEFAULT ''")
            }

        private val MIGRATION_4_5 =
            Migration(4, 5) { db ->
                db.execSQL("ALTER TABLE notes ADD COLUMN type TEXT NOT NULL DEFAULT 'MARKDOWN'")
                db.execSQL("ALTER TABLE notes ADD COLUMN payloadJson TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE notes ADD COLUMN metadata TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE notes ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            }
    }
}
