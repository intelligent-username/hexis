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

package com.loc.hexis.core.tasks

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface PomodoroRepo {
    suspend fun insertSession(session: PomodoroSession): Long

    suspend fun finishSession(
        id: Long,
        timeFinished: LocalDateTime,
        completed: Boolean,
        timeCompletedMinutes: Float,
    )

    suspend fun getTodayStats(): PomodoroStats

    fun getCompletedDates(): Flow<List<LocalDate>>

    suspend fun getEarliestSessionDate(): LocalDate?

    fun getSessionCountsByDay(): Flow<List<PomodoroDayCount>>

    suspend fun getSessionCountsByHabit(): List<Pair<Long?, Int>>

    suspend fun getAllSessions(): List<PomodoroSession>
}
