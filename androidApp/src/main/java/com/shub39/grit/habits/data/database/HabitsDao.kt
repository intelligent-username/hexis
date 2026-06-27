package com.shub39.grit.habits.data.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitsDao {
    @Query("SELECT * FROM habit_index WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): HabitEntity?

    @Query("SELECT * FROM habit_index") suspend fun getAllHabits(): List<HabitEntity>

    @Query("SELECT * FROM habit_index") fun getAllHabitsFlow(): Flow<List<HabitEntity>>

    @Upsert suspend fun upsertHabit(habitEntity: HabitEntity)

    @Query("DELETE FROM habit_index WHERE id = :habitId") suspend fun deleteHabit(habitId: Long)

    @Query("DELETE FROM habit_index") suspend fun deleteAllHabits()

    @Query("SELECT * FROM habit_index WHERE pomodoroLinked = 1")
    fun getPomodoroLinkedHabits(): Flow<List<HabitEntity>>
}