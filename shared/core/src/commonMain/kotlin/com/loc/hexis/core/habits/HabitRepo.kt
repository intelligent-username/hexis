package com.loc.hexis.core.habits

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface HabitRepo {
    suspend fun upsertHabit(habit: Habit)

    suspend fun deleteHabit(habitId: Long)

    suspend fun getHabits(): List<Habit>

    suspend fun getHabitById(id: Long): Habit?

    suspend fun getHabitStatuses(): List<HabitStatus>

    fun getHabitsWithAnalytics(): Flow<List<HabitWithAnalytics>>

    fun getCompletedHabitIds(): Flow<List<Long>>

    fun getOverallAnalytics(): Flow<OverallAnalytics>

    fun getHabitsWithStatus(): Flow<List<Pair<Habit, Boolean>>>

    suspend fun getStatusForHabit(id: Long): List<HabitStatus>

    suspend fun insertHabitStatus(habitStatus: HabitStatus)

    suspend fun deleteHabitStatus(habitId: Long, date: LocalDate)

    suspend fun getCompletedHabitsForDate(date: LocalDate): List<Habit>

    suspend fun incrementHabitProgress(
        habitId: Long,
        date: LocalDate,
        incrementBy: Double = 1.0,
    ): Double

    suspend fun decrementHabitProgress(
        habitId: Long,
        date: LocalDate,
        decrementBy: Double = 1.0,
    ): Double

    suspend fun getHabitProgress(habitId: Long, date: LocalDate): Double

    fun observePomodoroLinkedHabits(): Flow<List<Habit>>

    suspend fun isHabitCompleted(habitId: Long, date: LocalDate): Boolean

    fun getWeeklyPointsFlow(): Flow<List<WeeklyPoints>>

    fun getPointsTrend(): Flow<PointsTrend>
}
