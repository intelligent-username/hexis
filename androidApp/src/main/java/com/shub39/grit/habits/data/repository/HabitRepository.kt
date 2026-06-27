package com.shub39.grit.habits.data.repository

import com.shub39.grit.core.data.notification.HexisNotificationManager
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitRanking
import com.shub39.grit.core.habits.HabitRepo
import com.shub39.grit.core.habits.HabitStatus
import com.shub39.grit.core.habits.HabitWithAnalytics
import com.shub39.grit.core.habits.OverallAnalytics
import com.shub39.grit.core.interfaces.SettingsDatastore
import com.shub39.grit.core.now
import com.shub39.grit.habits.data.database.HabitStatusDao
import com.shub39.grit.habits.data.database.HabitsDao
import com.shub39.grit.habits.data.toHabit
import com.shub39.grit.habits.data.toHabitEntity
import com.shub39.grit.habits.data.toHabitStatus
import com.shub39.grit.habits.data.toHabitStatusEntity
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import org.koin.core.annotation.Single

@Single(binds = [HabitRepo::class])
@OptIn(ExperimentalTime::class)
class HabitRepository(
    private val habitDao: HabitsDao,
    private val habitStatusDao: HabitStatusDao,
    private val datastore: SettingsDatastore,
    private val notificationManager: HexisNotificationManager,
) : HabitRepo {

    private val habits =
        habitDao
            .getAllHabitsFlow()
            .map { habits -> habits.map { it.toHabit() }.sortedBy { it.index } }
            .flowOn(Dispatchers.IO)

    private val habitStatuses =
        habitStatusDao
            .getAllHabitStatuses()
            .map { habitStatuses -> habitStatuses.map { it.toHabitStatus() } }
            .flowOn(Dispatchers.IO)

    private val firstDayOfWeek = MutableStateFlow(DayOfWeek.MONDAY)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            datastore.getStartOfTheWeekPref().onEach { firstDayOfWeek.update { it } }.launchIn(this)
        }
    }

    override suspend fun upsertHabit(habit: Habit) {
        habitDao.upsertHabit(habit.toHabitEntity())
    }

    override suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteHabit(habitId)
    }

    override suspend fun getHabits(): List<Habit> {
        return habitDao.getAllHabits().map { it.toHabit() }
    }

    override suspend fun getHabitById(id: Long): Habit? {
        return habitDao.getHabitById(id)?.toHabit()
    }

    override suspend fun getHabitStatuses(): List<HabitStatus> {
        return habitStatusDao.getHabitStatuses().map { it.toHabitStatus() }
    }

    override fun getHabitsWithAnalytics(): Flow<List<HabitWithAnalytics>> {
        return combine(habits, habitStatuses, firstDayOfWeek) { habitsFlow, habitStatusesFlow, firstDay ->
                habitsFlow.map { habit ->
                    val habitStatusesForHabit = habitStatusesFlow.filter { it.habitId == habit.id }
                    val completedStatuses = filterCompletedStatuses(habit, habitStatusesForHabit)
                    val completedDates = completedStatuses.map { it.date }

                    HabitWithAnalytics(
                        habit = habit,
                        statuses = habitStatusesForHabit,
                        currentStreak =
                            countCurrentStreak(dates = completedDates, eligibleWeekdays = habit.days),
                        bestStreak = countBestStreak(dates = completedDates, eligibleWeekdays = habit.days),
                        weeklyComparisonData =
                            prepareLineChartData(
                                firstDay = firstDay,
                                habitStatuses = habitStatusesForHabit,
                            ),
                        weekDayFrequencyData = prepareWeekDayFrequencyData(dates = completedDates, firstDayOfWeek = firstDay),
                        startedDaysAgo = habit.time.date.daysUntil(LocalDate.now()).toLong(),
                        consistency = calculateConsistency(completedDates, habit.days),
                    )
                }
            }
            .flowOn(Dispatchers.Default)
    }

    override fun getCompletedHabitIds(): Flow<List<Long>> {
        return combine(habits, habitStatuses) { habitsFlow, habitStatusesFlow ->
            habitsFlow.mapNotNull { habit ->
                val todayStatus = habitStatusesFlow.find {
                    it.habitId == habit.id && it.date == LocalDate.now()
                }
                if (todayStatus != null && todayStatus.value >= (habit.targetValue ?: 1.0)) {
                    habit.id
                } else null
            }
        }.flowOn(Dispatchers.Default)
    }

    override fun getOverallAnalytics(): Flow<OverallAnalytics> {
        return combine(habits, habitStatuses, firstDayOfWeek) { habitsFlow, habitStatusesFlow, firstDay ->
                val allCompletedStatuses = habitsFlow.flatMap { habit ->
                    filterCompletedStatuses(habit, habitStatusesFlow.filter { it.habitId == habit.id })
                }

                val habitConsistencies =
                    habitsFlow.map { habit ->
                        val dates =
                            filterCompletedStatuses(habit, habitStatusesFlow.filter { it.habitId == habit.id }).map { it.date }
                        habit.title to calculateConsistency(dates, habit.days)
                    }

                val consistencies = habitConsistencies.map { it.second }
                val overallConsistency =
                    if (consistencies.isNotEmpty()) consistencies.average().toFloat() else 0f

                val topHabits =
                    habitConsistencies
                        .filter { it.second > 0f }
                        .sortedByDescending { it.second }
                        .take(3)
                        .map { HabitRanking(it.first, it.second) }

                OverallAnalytics(
                    heatMapData = prepareHeatMapData(allCompletedStatuses.toList()),
                    weekDayFrequencyData =
                        prepareWeekDayFrequencyData(allCompletedStatuses.map { it.date }, firstDay),
                    consistency = overallConsistency,
                    topHabits = topHabits,
                )
            }
            .flowOn(Dispatchers.Default)
    }

    override fun getHabitsWithStatus(): Flow<List<Pair<Habit, Boolean>>> {
        return habits.combine(habitStatuses) { habitsFlow, statusFlow ->
            habitsFlow.map { habit ->
                val todayStatus = statusFlow.find {
                    it.habitId == habit.id && it.date == LocalDate.now()
                }
                habit to (todayStatus != null && todayStatus.value >= (habit.targetValue ?: 1.0))
            }
        }
    }

    override suspend fun getStatusForHabit(id: Long): List<HabitStatus> {
        return habitStatusDao.getStatusForHabit(id).map { it.toHabitStatus() }
    }

    override suspend fun insertHabitStatus(habitStatus: HabitStatus) {
        habitStatusDao.insertHabitStatus(habitStatus.toHabitStatusEntity())

        if (habitStatus.date == LocalDate.now()) {
            notificationManager.cancelNotification(habitId = habitStatus.habitId.toInt())
        }
    }

    override suspend fun deleteHabitStatus(habitId: Long, date: LocalDate) {
        habitStatusDao.deleteStatus(habitId, date)
    }

    override suspend fun getCompletedHabitsForDate(date: LocalDate): List<Habit> {
        val completedStatuses = habitStatusDao.getCompletedStatuses(date)
        return completedStatuses.mapNotNull { habitDao.getHabitById(it.habitId)?.toHabit() }
    }

    override suspend fun incrementHabitProgress(habitId: Long, date: LocalDate, incrementBy: Double): Double {
        val currentValue = habitStatusDao.getProgressOrDefault(habitId, date)
        val newValue = currentValue + incrementBy
        val existingId = habitStatusDao.getStatusId(habitId, date) ?: 0L
        habitStatusDao.upsert(
            com.shub39.grit.habits.data.database.HabitStatusEntity(
                id = existingId,
                habitId = habitId,
                date = date,
                value = newValue,
            )
        )

        if (date == LocalDate.now()) {
            notificationManager.cancelNotification(habitId = habitId.toInt())
        }

        return newValue
    }

    override suspend fun getHabitProgress(habitId: Long, date: LocalDate): Double {
        return habitStatusDao.getProgressOrDefault(habitId, date)
    }

    override fun observePomodoroLinkedHabits(): Flow<List<Habit>> {
        return habitDao.getPomodoroLinkedHabits().map { entities ->
            entities.map { it.toHabit() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun isHabitCompleted(habitId: Long, date: LocalDate): Boolean {
        val habit = habitDao.getHabitById(habitId)?.toHabit() ?: return false
        val value = habitStatusDao.getProgressOrDefault(habitId, date)
        return value >= (habit.targetValue ?: 1.0)
    }
}
