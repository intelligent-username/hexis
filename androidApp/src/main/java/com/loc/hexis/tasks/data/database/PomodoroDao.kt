/*
 * Copyright (C) 2025-2026 Hexis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.loc.hexis.tasks.data.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.loc.hexis.core.tasks.PomodoroStats
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

data class EpochDayCount(val epochDay: Long, val count: Int)

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

    @Query(
        "SELECT CAST(COUNT(*) AS INTEGER) AS sessionCount, CAST(COALESCE(SUM(timeCompletedMinutes), 0.0) AS REAL) AS totalMinutes FROM pomodoro_sessions WHERE timeStarted >= :todayStart"
    )
    suspend fun getTodayStats(todayStart: LocalDateTime): PomodoroStats

    @Query(
        "SELECT DISTINCT CAST(timeStarted / 86400 AS INTEGER) FROM pomodoro_sessions WHERE completed = 1 ORDER BY timeStarted DESC"
    )
    fun getCompletedDates(): Flow<List<Long>>

    @Query(
        """
        SELECT CAST(timeStarted / 86400 AS INTEGER) AS epochDay,
               COUNT(*) AS count
        FROM pomodoro_sessions
        WHERE completed = 1
        GROUP BY epochDay
        ORDER BY epochDay
    """
    )
    fun getSessionCountsByDay(): Flow<List<EpochDayCount>>

    @Query("SELECT MIN(timeStarted) FROM pomodoro_sessions")
    suspend fun getEarliestSessionDate(): LocalDateTime?

    @Query(
        """
        SELECT linkedHabitId, CAST(COUNT(*) AS INTEGER) AS count
        FROM pomodoro_sessions
        WHERE completed = 1
        GROUP BY linkedHabitId
    """
    )
    suspend fun getSessionCountsByHabit(): List<HabitSessionCount>
}

data class HabitSessionCount(val linkedHabitId: Long?, val count: Int)
