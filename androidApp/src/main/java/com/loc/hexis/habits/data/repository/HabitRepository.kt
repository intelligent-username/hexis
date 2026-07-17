package com.loc.hexis.habits.data.repository

import com.loc.hexis.core.data.notification.HexisNotificationManager
import com.loc.hexis.core.habits.DisplayMode
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.habits.HabitRanking
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.habits.HabitStatus
import com.loc.hexis.core.habits.HabitWithAnalytics
import com.loc.hexis.core.habits.OverallAnalytics
import com.loc.hexis.core.habits.PointsTrend
import com.loc.hexis.core.habits.WeeklyPoints
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.now
import com.loc.hexis.habits.data.database.HabitStatusDao
import com.loc.hexis.habits.data.database.HabitsDao
import com.loc.hexis.habits.data.toHabit
import com.loc.hexis.habits.data.toHabitEntity
import com.loc.hexis.habits.data.toHabitStatus
import com.loc.hexis.habits.data.toHabitStatusEntity
import kotlin.math.round
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
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
        return combine(habits, habitStatuses, firstDayOfWeek) {
                habitsFlow,
                habitStatusesFlow,
                firstDay ->
                habitsFlow.map { habit ->
                    val habitStatusesForHabit = habitStatusesFlow.filter { it.habitId == habit.id }
                    val completedStatuses = filterCompletedStatuses(habit, habitStatusesForHabit)
                    val completedDates = completedStatuses.map { it.date }

                    val pointsSummary = computePointsSummary(habit, completedStatuses, firstDay)

                    HabitWithAnalytics(
                        habit = habit,
                        statuses = habitStatusesForHabit,
                        currentStreak =
                            countCurrentStreak(
                                dates = completedDates,
                                eligibleWeekdays = habit.days,
                            ),
                        bestStreak =
                            countBestStreak(dates = completedDates, eligibleWeekdays = habit.days),
                        weeklyComparisonData =
                            prepareLineChartData(
                                firstDay = firstDay,
                                habitStatuses = habitStatusesForHabit,
                                targetValue = habit.targetValue ?: 1.0,
                            ),
                        weekDayFrequencyData =
                            prepareWeekDayFrequencyData(
                                dates = completedDates,
                                firstDayOfWeek = firstDay,
                            ),
                        startedDaysAgo = habit.time.date.daysUntil(LocalDate.now()).toLong(),
                        consistency = calculateConsistency(completedDates, habit.days),
                        pointsSummary = pointsSummary,
                    )
                }
            }
            .flowOn(Dispatchers.Default)
    }

    override fun getCompletedHabitIds(): Flow<List<Long>> {
        return combine(habits, habitStatuses) { habitsFlow, habitStatusesFlow ->
                habitsFlow.mapNotNull { habit ->
                    val todayStatus =
                        habitStatusesFlow.find {
                            it.habitId == habit.id && it.date == LocalDate.now()
                        }
                    if (todayStatus != null && todayStatus.value >= (habit.targetValue ?: 1.0)) {
                        habit.id
                    } else null
                }
            }
            .flowOn(Dispatchers.Default)
    }

    override fun getOverallAnalytics(): Flow<OverallAnalytics> {
        return combine(habits, habitStatuses, firstDayOfWeek) {
                habitsFlow,
                habitStatusesFlow,
                firstDay ->
                val allCompletedStatuses =
                    habitsFlow.flatMap { habit ->
                        filterCompletedStatuses(
                            habit,
                            habitStatusesFlow.filter { it.habitId == habit.id },
                        )
                    }

                val habitConsistencies =
                    habitsFlow.map { habit ->
                        val dates =
                            filterCompletedStatuses(
                                    habit,
                                    habitStatusesFlow.filter { it.habitId == habit.id },
                                )
                                .map { it.date }
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

                val allPointsSummaries =
                    habitsFlow.map { habit ->
                        val completed =
                            filterCompletedStatuses(
                                habit,
                                habitStatusesFlow.filter { it.habitId == habit.id },
                            )
                        computePointsSummary(habit, completed, firstDay)
                    }
                val totalPoints = allPointsSummaries.sumOf { it.totalPoints }
                val aggregateWeeklyHistory =
                    if (allPointsSummaries.isNotEmpty()) {
                        (0..52).map { weekIdx ->
                            allPointsSummaries.sumOf {
                                it.weeklyPointsHistory.getOrElse(weekIdx) { 0 }
                            }
                        }
                    } else emptyList()

                val habitBestStreaks =
                    habitsFlow.map { habit ->
                        val completedDates =
                            filterCompletedStatuses(
                                    habit,
                                    habitStatusesFlow.filter { it.habitId == habit.id },
                                )
                                .map { it.date }
                        countBestStreak(dates = completedDates, eligibleWeekdays = habit.days)
                    }
                val longestStreak = habitBestStreaks.maxOrNull() ?: 0

                OverallAnalytics(
                    heatMapData = prepareHeatMapData(allCompletedStatuses.toList()),
                    weekDayFrequencyData =
                        prepareWeekDayFrequencyData(allCompletedStatuses.map { it.date }, firstDay),
                    consistency = overallConsistency,
                    topHabits = topHabits,
                    totalPoints = totalPoints,
                    weeklyPointsHistory = aggregateWeeklyHistory,
                    longestStreak = longestStreak,
                )
            }
            .flowOn(Dispatchers.Default)
    }

    override fun getWeeklyPointsFlow(): Flow<List<WeeklyPoints>> =
        combine(habits, habitStatuses, firstDayOfWeek) { habitsFlow, habitStatusesFlow, firstDay ->
                computeWeeklyPoints(habitsFlow, habitStatusesFlow, firstDay)
            }
            .flowOn(Dispatchers.Default)
            .distinctUntilChanged()

    override fun getPointsTrend(): Flow<PointsTrend> =
        combine(habits, habitStatuses, firstDayOfWeek) { habits, statuses, firstDay ->
                val weeklyPoints = computeWeeklyPoints(habits, statuses, firstDay)
                val trend = computePointsTrend(weeklyPoints)

                val today = LocalDate.now()
                val todayDow = today.dayOfWeek.isoDayNumber
                val firstDow = firstDay.isoDayNumber
                val diff =
                    if (todayDow >= firstDow) todayDow - firstDow else 7 + todayDow - firstDow
                val weekStart = today.minus(diff, DateTimeUnit.DAY)

                val currentPartial = computePointsForPeriod(habits, statuses, weekStart, today)
                val prevWeekStart = weekStart.minus(7, DateTimeUnit.DAY)
                val prevToday = today.minus(7, DateTimeUnit.DAY)
                val previousPartial =
                    computePointsForPeriod(habits, statuses, prevWeekStart, prevToday)

                val partialNetChange = currentPartial - previousPartial
                val partialNetChangePercent =
                    if (previousPartial > 0) (partialNetChange.toFloat() / previousPartial * 100)
                    else 0f

                val offsetDays = weekStart.daysUntil(today)
                val historicalPartials = weeklyPoints.dropLast(1).map { wp ->
                    computePointsForPeriod(
                        habits,
                        statuses,
                        wp.weekStart,
                        wp.weekStart.plus(offsetDays, DateTimeUnit.DAY),
                    )
                }

                val avgWeeklyPartial = if (historicalPartials.isNotEmpty()) {
                    historicalPartials.average().toFloat()
                } else {
                    0f
                }
                val bestWeekPartial = if (historicalPartials.isNotEmpty()) {
                    historicalPartials.max()
                } else {
                    0
                }

                trend.copy(
                    currentPartialPoints = currentPartial,
                    previousPartialPoints = previousPartial,
                    partialNetChange = partialNetChange,
                    partialNetChangePercent = partialNetChangePercent,
                    averageWeeklyPoints = avgWeeklyPartial,
                    bestWeekPoints = bestWeekPartial,
                )
            }
            .flowOn(Dispatchers.Default)
            .distinctUntilChanged()

    override fun getHabitsWithStatus(): Flow<List<Pair<Habit, Boolean>>> {
        return habits.combine(habitStatuses) { habitsFlow, statusFlow ->
            habitsFlow.map { habit ->
                val todayStatus =
                    statusFlow.find { it.habitId == habit.id && it.date == LocalDate.now() }
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

    override suspend fun incrementHabitProgress(
        habitId: Long,
        date: LocalDate,
        incrementBy: Double,
    ): Double {
        val currentValue = habitStatusDao.getProgressOrDefault(habitId, date)
        val newValue = currentValue + incrementBy
        habitStatusDao.deleteStatus(habitId, date)
        habitStatusDao.upsert(
            com.loc.hexis.habits.data.database.HabitStatusEntity(
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

    override suspend fun decrementHabitProgress(
        habitId: Long,
        date: LocalDate,
        decrementBy: Double,
    ): Double {
        val existing = habitStatusDao.getStatus(habitId, date) ?: return 0.0
        val newValue = existing.value - decrementBy
        if (newValue <= 0.0) {
            habitStatusDao.deleteStatus(habitId, date)
        } else {
            habitStatusDao.upsert(existing.copy(value = newValue))
        }
        return newValue.coerceAtLeast(0.0)
    }

    override suspend fun getHabitProgress(habitId: Long, date: LocalDate): Double {
        return habitStatusDao.getProgressOrDefault(habitId, date)
    }

    override fun observePomodoroLinkedHabits(): Flow<List<Habit>> {
        return habitDao
            .getPomodoroLinkedHabits()
            .map { entities -> entities.map { it.toHabit() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun isHabitCompleted(habitId: Long, date: LocalDate): Boolean {
        val habit = habitDao.getHabitById(habitId)?.toHabit() ?: return false
        val value = habitStatusDao.getProgressOrDefault(habitId, date)
        return value >= (habit.targetValue ?: 1.0)
    }

    private fun computeWeeklyPoints(
        habits: List<Habit>,
        statuses: List<HabitStatus>,
        firstDayOfWeek: DayOfWeek,
    ): List<WeeklyPoints> {
        val now = LocalDate.now()
        val weeksBack = 26
        val weekStarts =
            (0 until weeksBack)
                .map { i ->
                    val weekStart = now.minus(i, DateTimeUnit.WEEK)
                    val diff = firstDayOfWeek.isoDayNumber - weekStart.dayOfWeek.isoDayNumber
                    val shift = if (diff > 0) diff - 7 else diff
                    weekStart.plus(shift, DateTimeUnit.DAY)
                }
                .reversed()

        return weekStarts.map { weekStart ->
            val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
            val weekStatuses = statuses.filter { it.date in weekStart..weekEnd }
            val activeHabits = habits.filter { it.time.date <= weekEnd }

            var totalPoints = 0
            var completedCount = 0
            var maxPossible = 0
            var streakBonus = 0
            var perfectWeekBonus = 0

            activeHabits.forEach { habit ->
                val habitStatuses = weekStatuses.filter { it.habitId == habit.id }
                val eligibleDays = habit.days
                val weekEligibleDays =
                    eligibleDays.filter { day ->
                        val d = weekStart.daysUntil(weekEnd)
                        (0..d).any {
                            weekStart.plus(it.toLong(), DateTimeUnit.DAY).dayOfWeek == day
                        }
                    }
                maxPossible += weekEligibleDays.size * 10

                if (habit.displayMode == DisplayMode.CHECKBOX) {
                    val completedDays = habitStatuses.map { it.date }.toSet()
                    val completedEligible =
                        weekEligibleDays.count { day ->
                            val datesInWeek =
                                (0..weekStart.daysUntil(weekEnd)).map {
                                    weekStart.plus(it.toLong(), DateTimeUnit.DAY)
                                }
                            datesInWeek.any { it.dayOfWeek == day && it in completedDays }
                        }
                    totalPoints += completedEligible * 10
                    if (completedEligible > 0) completedCount++

                    val streak =
                        countCurrentStreak(
                            dates = statuses.filter { it.habitId == habit.id }.map { it.date },
                            eligibleWeekdays = habit.days,
                        )
                    streakBonus += minOf(streak * 3, 30)

                    if (
                        completedEligible == weekEligibleDays.size && weekEligibleDays.isNotEmpty()
                    ) {
                        perfectWeekBonus += 50
                    }
                } else {
                    habitStatuses.forEach { status ->
                        val ratio = (status.value / (habit.targetValue ?: 1.0)).coerceIn(0.0, 1.0)
                        totalPoints += round(ratio * 10).toInt()
                    }
                    if (habitStatuses.any { it.value >= (habit.targetValue ?: 1.0) })
                        completedCount++
                }
            }

            WeeklyPoints(
                weekStart = weekStart,
                weekEnd = weekEnd,
                pointsEarned = totalPoints + streakBonus + perfectWeekBonus,
                habitsCompleted = completedCount,
                totalPossiblePoints = maxPossible,
                completionRate =
                    if (maxPossible > 0)
                        (totalPoints + streakBonus + perfectWeekBonus).toFloat() / maxPossible
                    else 0f,
                streakBonusPoints = streakBonus,
                perfectWeekBonusPoints = perfectWeekBonus,
            )
        }
    }

    private fun computePointsTrend(weeklyPoints: List<WeeklyPoints>): PointsTrend {
        if (weeklyPoints.isEmpty()) return PointsTrend.empty

        val current = weeklyPoints.last()
        val previous =
            weeklyPoints.getOrNull(weeklyPoints.size - 2)
                ?: WeeklyPoints(
                    weekStart = current.weekStart.minus(1, DateTimeUnit.WEEK),
                    weekEnd = current.weekEnd.minus(1, DateTimeUnit.WEEK),
                    pointsEarned = 0,
                    habitsCompleted = 0,
                    totalPossiblePoints = 0,
                    completionRate = 0f,
                )

        val netChange = current.pointsEarned - previous.pointsEarned
        val netChangePercent =
            if (previous.pointsEarned > 0) (netChange.toFloat() / previous.pointsEarned * 100)
            else 0f

        val totalAllTime = weeklyPoints.sumOf { it.pointsEarned }
        val avgWeekly =
            if (weeklyPoints.isNotEmpty()) totalAllTime.toFloat() / weeklyPoints.size else 0f
        val bestWeek = weeklyPoints.maxByOrNull { it.pointsEarned }?.pointsEarned ?: 0

        var streakWeeks = 0
        for (wp in weeklyPoints.reversed()) {
            if (wp.pointsEarned > 0) streakWeeks++ else break
        }

        return PointsTrend(
            weeklyPoints = weeklyPoints,
            currentWeekPoints = current.pointsEarned,
            previousWeekPoints = previous.pointsEarned,
            netChange = netChange,
            netChangePercent = netChangePercent,
            totalPointsAllTime = totalAllTime,
            averageWeeklyPoints = avgWeekly,
            bestWeekPoints = bestWeek,
            currentStreakWeeks = streakWeeks,
        )
    }

    private fun computePointsForPeriod(
        habits: List<Habit>,
        statuses: List<HabitStatus>,
        from: LocalDate,
        to: LocalDate,
    ): Int {
        var totalPoints = 0
        habits.forEach { habit ->
            val habitStatuses = statuses.filter { it.habitId == habit.id && it.date in from..to }
            val eligibleDays = habit.days
            val daysInRange =
                (0..from.daysUntil(to)).map { from.plus(it.toLong(), DateTimeUnit.DAY) }
            val eligibleDatesInRange = daysInRange.filter { it.dayOfWeek in eligibleDays }

            if (habit.displayMode == DisplayMode.CHECKBOX) {
                val completedDays = habitStatuses.map { it.date }.toSet()
                val completed = eligibleDatesInRange.count { it in completedDays }
                totalPoints += completed * 10
            } else {
                habitStatuses.forEach { status ->
                    val ratio = (status.value / (habit.targetValue ?: 1.0)).coerceIn(0.0, 1.0)
                    totalPoints += round(ratio * 10).toInt()
                }
            }
        }
        return totalPoints
    }
}
