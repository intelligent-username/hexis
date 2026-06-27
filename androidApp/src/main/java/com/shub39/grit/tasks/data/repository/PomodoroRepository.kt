package com.shub39.grit.tasks.data.repository

import com.shub39.grit.core.now
import com.shub39.grit.core.tasks.PomodoroRepo
import com.shub39.grit.core.tasks.PomodoroSession
import com.shub39.grit.core.tasks.PomodoroStats
import com.shub39.grit.tasks.data.database.PomodoroDao
import com.shub39.grit.tasks.data.toPomodoroSession
import com.shub39.grit.tasks.data.toPomodoroSessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.core.annotation.Single

@Single(binds = [PomodoroRepo::class])
class PomodoroRepository(
    private val pomodoroDao: PomodoroDao,
) : PomodoroRepo {

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
        return withContext(Dispatchers.IO) {
            pomodoroDao.getTodayStats(todayStart)
        }
    }

    override fun getCompletedDates(): Flow<List<LocalDate>> {
        return pomodoroDao.getCompletedDates().map { epochs ->
            epochs.map { LocalDate.fromEpochDays(it.toInt()) }
        }
    }

    override suspend fun getEarliestSessionDate(): LocalDate? {
        return pomodoroDao.getEarliestSessionDate()?.date
    }
}
