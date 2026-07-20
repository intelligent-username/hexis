package com.loc.hexis.tasks.data.repository

import com.loc.hexis.core.now
import com.loc.hexis.core.tasks.PomodoroDayCount
import com.loc.hexis.core.tasks.PomodoroRepo
import com.loc.hexis.core.tasks.PomodoroSession
import com.loc.hexis.core.tasks.PomodoroStats
import com.loc.hexis.tasks.data.database.PomodoroDao
import com.loc.hexis.tasks.data.toPomodoroSession
import com.loc.hexis.tasks.data.toPomodoroSessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.core.annotation.Single

@Single(binds = [PomodoroRepo::class])
class PomodoroRepository(private val pomodoroDao: PomodoroDao) : PomodoroRepo {

    override suspend fun insertSession(session: PomodoroSession): Long {
        return pomodoroDao.insert(session.toPomodoroSessionEntity())
    }

    override suspend fun finishSession(
        id: Long,
        timeFinished: LocalDateTime,
        completed: Boolean,
        timeCompletedMinutes: Float,
    ) {
        pomodoroDao.finish(id, timeFinished, completed, timeCompletedMinutes)
    }

    override suspend fun getTodayStats(): PomodoroStats {
        val now = LocalDateTime.now()
        val todayStart = LocalDateTime(now.year, now.month, now.day, 0, 0)
        return withContext(Dispatchers.IO) { pomodoroDao.getTodayStats(todayStart) }
    }

    override fun getCompletedDates(): Flow<List<LocalDate>> {
        return pomodoroDao.getCompletedDates().map { epochs ->
            epochs.map { LocalDate.fromEpochDays(it.toInt()) }
        }
    }

    override suspend fun getEarliestSessionDate(): LocalDate? {
        return pomodoroDao.getEarliestSessionDate()?.date
    }

    override fun getSessionCountsByDay(): Flow<List<PomodoroDayCount>> {
        return pomodoroDao
            .getSessionCountsByDay()
            .map { list ->
                list.map {
                    PomodoroDayCount(
                        date = LocalDate.fromEpochDays(it.epochDay.toInt()),
                        count = it.count,
                    )
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getSessionCountsByHabit(): List<Pair<Long?, Int>> {
        return withContext(Dispatchers.IO) {
            pomodoroDao.getSessionCountsByHabit().map { it.linkedHabitId to it.count }
        }
    }

    override suspend fun getAllSessions(): List<PomodoroSession> {
        return withContext(Dispatchers.IO) { pomodoroDao.getAll().map { it.toPomodoroSession() } }
    }
}
