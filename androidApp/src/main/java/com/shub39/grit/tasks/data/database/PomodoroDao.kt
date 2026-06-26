/*
 * Copyright (C) 2026  Shubham Gorai
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
package com.shub39.grit.tasks.data.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import com.shub39.grit.core.tasks.PomodoroStats
import kotlinx.datetime.LocalDateTime

@Dao
interface PomodoroDao {
    @Upsert suspend fun insert(session: PomodoroSessionEntity): Long

    @Query("UPDATE pomodoro_sessions SET timeFinished = :timeFinished, completed = :completed, timeCompletedMinutes = :timeCompletedMinutes WHERE id = :id")
    suspend fun finish(id: Long, timeFinished: LocalDateTime, completed: Boolean, timeCompletedMinutes: Float)

    @Query("SELECT COUNT(*) AS sessionCount, COALESCE(SUM(timeCompletedMinutes), 0) AS totalMinutes FROM pomodoro_sessions WHERE timeStarted >= :todayStart")
    suspend fun getTodayStats(todayStart: LocalDateTime): PomodoroStats

    @Query("SELECT DISTINCT CAST(timeStarted / 86400 AS INTEGER) FROM pomodoro_sessions WHERE completed = 1 ORDER BY timeStarted DESC")
    fun getCompletedDates(): Flow<List<Long>>

    @Query("SELECT MIN(timeStarted) FROM pomodoro_sessions")
    suspend fun getEarliestSessionDate(): LocalDateTime?
}
