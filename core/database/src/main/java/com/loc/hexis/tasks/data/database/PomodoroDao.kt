package com.loc.hexis.tasks.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface PomodoroDao {
    @Upsert suspend fun insert(session: PomodoroSessionEntity): Long

    @Query(
        "UPDATE pomodoro_sessions SET timeFinished = :timeFinished, completed = :completed, timeCompletedMinutes = :timeCompletedMinutes WHERE id = :id"
    )
    suspend fun finish(
        id: Long,
        timeFinished: LocalDateTime,
        completed: Boolean,
        timeCompletedMinutes: Float,
    )

    @Query("SELECT * FROM pomodoro_sessions ORDER BY timeStarted ASC")
    fun getAllFlow(): Flow<List<PomodoroSessionEntity>>

    @Query(
        """
        SELECT linkedHabitId, CAST(COUNT(*) AS INTEGER) AS count
        FROM pomodoro_sessions
        GROUP BY linkedHabitId
    """
    )
    suspend fun getSessionCountsByHabit(): List<HabitSessionCount>

    @Query("SELECT * FROM pomodoro_sessions ORDER BY timeStarted ASC")
    suspend fun getAll(): List<PomodoroSessionEntity>
}

data class HabitSessionCount(val linkedHabitId: Long?, val count: Int)
