package com.shub39.grit.habits.data.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface HabitStatusDao {
    @Query("SELECT * FROM habit_status") suspend fun getHabitStatuses(): List<HabitStatusEntity>

    @Query("SELECT * FROM habit_status WHERE date = :date")
    suspend fun getCompletedStatuses(date: LocalDate): List<HabitStatusEntity>

    @Query("SELECT * FROM habit_status") fun getAllHabitStatuses(): Flow<List<HabitStatusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitStatus(habitStatusEntity: HabitStatusEntity)

    @Query("SELECT * FROM habit_status WHERE habitId = :habitId")
    suspend fun getStatusForHabit(habitId: Long): List<HabitStatusEntity>

    @Query("DELETE FROM habit_status WHERE habitId = :habitId AND date = :date")
    suspend fun deleteStatus(habitId: Long, date: LocalDate)

    @Query("DELETE FROM habit_status") suspend fun deleteAllHabitStatus()

    @Upsert
    suspend fun upsert(habitStatusEntity: HabitStatusEntity)

    @Query("SELECT value FROM habit_status WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getProgress(habitId: Long, date: LocalDate): Double?

    @Query("SELECT * FROM habit_status WHERE habitId = :habitId AND date = :date ORDER BY id DESC LIMIT 1")
    suspend fun getStatus(habitId: Long, date: LocalDate): HabitStatusEntity?

    @Query("SELECT COALESCE(SUM(value), 0.0) FROM habit_status WHERE habitId = :habitId AND date = :date")
    suspend fun getProgressOrDefault(habitId: Long, date: LocalDate): Double
}