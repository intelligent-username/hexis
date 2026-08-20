
package com.loc.hexis.habits.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.annotation.Single

@Single
class HabitDbFactory(private val context: Context) {
    fun create(): RoomDatabase.Builder<HabitDatabase> {
        val appContext = context.applicationContext

        return Room.databaseBuilder(appContext, HabitDatabase::class.java, HabitDatabase.DB_NAME)
    }
}
