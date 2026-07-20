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
