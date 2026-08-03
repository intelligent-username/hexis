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

package com.loc.hexis.shared.ui.task.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HeatMapCalendar
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapCalendarState
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.now
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.habits.countBestStreak
import com.loc.hexis.core.habits.countCurrentStreak
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.interfaces.ThemeDatastore
import com.loc.hexis.core.tasks.PomodoroDayCount
import com.loc.hexis.core.tasks.PomodoroRepo
import com.loc.hexis.shared.ui.components.HexisBottomSheet
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.close
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroAnalytics(onDismiss: () -> Unit, onSelectHabit: (Long?, String) -> Unit = { _, _ -> }) {
    val repo: PomodoroRepo = koinInject()

    val settingsDatastore: SettingsDatastore = koinInject()
    val themeDatastore: ThemeDatastore = koinInject()
    val isAmoled by themeDatastore.getAmoledPref().collectAsState(initial = false)
    val showPieChart by
        settingsDatastore.getShowPomodoroPieChartPref().collectAsState(initial = true)

    val dayCounts by repo.getSessionCountsByDay().collectAsState(initial = emptyList())
    val dayMinutes by repo.getSessionMinutesByDay().collectAsState(initial = emptyList())
    val dayMinutesMap = remember(dayMinutes) { dayMinutes.associateBy { it.date } }
    val maxMinutes = remember(dayMinutes) { dayMinutes.maxOfOrNull { it.totalMinutes } ?: 0f }
    val dates = remember(dayCounts) { dayCounts.map { it.date } }

    val totalSessions = dayCounts.sumOf { it.count }
    val totalMinutes =
        remember(dayMinutes) { dayMinutes.sumOf { it.totalMinutes.toDouble() }.toFloat() }
    val currentStreak = remember(dates) { countCurrentStreak(dates) }
    val bestStreak = remember(dates) { countBestStreak(dates) }

    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }

    val startOfWeekPref by
        settingsDatastore.getStartOfTheWeekPref().collectAsState(initial = DayOfWeek.MONDAY)

    val firstLaunchDate by
        settingsDatastore.getFirstLaunchDate().collectAsState(initial = null)

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember(firstLaunchDate) {
        firstLaunchDate?.let { YearMonth(it.year, it.month) } ?: currentMonth
    }
    val heatMapState: HeatMapCalendarState =
        key(startMonth, startOfWeekPref) {
            rememberHeatMapCalendarState(
                startMonth = startMonth,
                endMonth = currentMonth,
                firstVisibleMonth = currentMonth,
                firstDayOfWeek = startOfWeekPref,
            )
        }

    Scaffold(
        containerColor = if (isAmoled) Color.Black else MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                colors =
                    if (isAmoled) {
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black,
                            scrolledContainerColor = Color.Black,
                        )
                    } else {
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                        )
                    },
                title = { Text(text = "Session History", fontFamily = flexFontEmphasis()) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.close),
                            contentDescription = "Close",
                        )
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (dayCounts.isNotEmpty()) {
                StatsRow(
                    totalSessions = totalSessions,
                    totalMinutes = totalMinutes,
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                )

                if (showPieChart) {
                    HabitBreakdownChart(
                        onSelectHabit = { habitId, title ->
                            onSelectHabit(habitId, title)
                            onDismiss()
                        }
                    )
                }

                ThisWeekRow(dayMinutes = dayMinutes, startOfWeekPref = startOfWeekPref)

                SessionHeatMap(
                    heatMapState = heatMapState,
                    dayMinutesMap = dayMinutesMap,
                    maxMinutes = maxMinutes,
                    onDayClick = { selectedDay = it },
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Complete your first Pomodoro session to see history here.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = flexFontRounded(),
                    )
                }
            }
        }
    }

    selectedDay?.let { date ->
        val entry = dayMinutesMap[date]
        val minutes = entry?.totalMinutes ?: 0f
        val whole = minutes.toInt()
        val sessions = entry?.count ?: 0
        HexisBottomSheet(onDismissRequest = { selectedDay = null }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = date.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = flexFontRounded(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$whole minute${if (whole != 1) "s" else ""}",
                    style =
                        MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = flexFontRounded(),
                        ),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$sessions session${if (sessions != 1) "s" else ""}",
                    style =
                        MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontRounded()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatsRow(totalSessions: Int, totalMinutes: Float, currentStreak: Int, bestStreak: Int) {
    val totalHours = (totalMinutes / 60).toInt()
    val remMins = (totalMinutes % 60).toInt()
    val formattedTime =
        if (totalHours > 0) "${totalHours}h ${remMins}m" else "${totalMinutes.toInt()}m"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatPill(value = "$totalSessions", label = "sessions", modifier = Modifier.weight(1f))
            StatPill(value = formattedTime, label = "time worked", modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatPill(value = "$currentStreak", label = "day streak", modifier = Modifier.weight(1f))
            StatPill(value = "$bestStreak", label = "best streak", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatPill(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = flexFontRounded(),
                    ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = flexFontRounded(),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun ThisWeekRow(dayMinutes: List<PomodoroDayCount>, startOfWeekPref: DayOfWeek) {
    val today = LocalDate.now()
    val daysFromStart = (today.dayOfWeek.isoDayNumber - startOfWeekPref.isoDayNumber + 7) % 7
    val daysElapsed = daysFromStart + 1
    val startOfWeek = today.minus(daysFromStart, DateTimeUnit.DAY)
    val weekMinutes =
        dayMinutes.filter { it.date in startOfWeek..today }.sumOf { it.totalMinutes.toDouble() }
    val whole = weekMinutes.toInt()
    val avg = (weekMinutes / daysElapsed.toDouble()).toInt()

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "This week: ${whole}m",
            style = MaterialTheme.typography.titleSmall,
            fontFamily = flexFontRounded(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "avg: ${avg}m/d",
            style = MaterialTheme.typography.titleSmall,
            fontFamily = flexFontRounded(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SessionHeatMap(
    heatMapState: HeatMapCalendarState,
    dayMinutesMap: Map<LocalDate, PomodoroDayCount>,
    maxMinutes: Float,
    onDayClick: (LocalDate) -> Unit,
) {
    val today = LocalDate.now()

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        HeatMapCalendar(
            state = heatMapState,
            contentPadding = PaddingValues(8.dp),
            monthHeader = { calendarMonth ->
                Text(
                    text =
                        "${calendarMonth.yearMonth.month.name.take(3)} ${calendarMonth.yearMonth.year}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = flexFontRounded(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
                )
            },
            dayContent = { day, _ ->
                if (day.date > today) return@HeatMapCalendar

                val minutes = dayMinutesMap[day.date]?.totalMinutes ?: 0f
                val alpha = if (maxMinutes > 0) (minutes / maxMinutes).coerceIn(0f, 1f) else 0f

                val bgColor =
                    when {
                        day.date > today -> Color.Transparent
                        minutes == 0f -> MaterialTheme.colorScheme.surfaceContainerHighest
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                    }

                val textColor =
                    when {
                        minutes == 0f -> MaterialTheme.colorScheme.onSurface
                        alpha > 0.5f -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.primary
                    }

                Box(
                    modifier =
                        Modifier.size(36.dp)
                            .background(color = bgColor, shape = RoundedCornerShape(4.dp))
                            .clickable { onDayClick(day.date) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (minutes > 0f) {
                        Text(
                            text = minutes.toInt().toString(),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontFamily = flexFontRounded(),
                                ),
                            color = textColor,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun HabitBreakdownChart(onSelectHabit: (Long?, String) -> Unit = { _, _ -> }) {
    val repo: PomodoroRepo = koinInject()
    val habitRepo: HabitRepo = koinInject()

    data class HabitCount(val id: Long?, val count: Int, val title: String)

    var displayData by remember { mutableStateOf<List<HabitCount>>(emptyList()) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val counts = repo.getSessionCountsByHabit()
        val allEntries =
            counts
                .map { (id, c) ->
                    val title =
                        if (id != null) {
                            (habitRepo.getHabitById(id))?.title ?: "Unknown"
                        } else "Misc."
                    HabitCount(id, c, title)
                }
                .sortedByDescending { it.count }

        displayData =
            if (allEntries.size > 4) {
                val top3 = allEntries.take(3)
                val remainingCount = allEntries.drop(3).sumOf { it.count }
                if (remainingCount > 0) {
                    top3 + HabitCount(null, remainingCount, "Other")
                } else {
                    top3
                }
            } else {
                allEntries
            }
    }

    if (displayData.isEmpty()) return

    val total = displayData.sumOf { it.count }.toFloat()

    val palette =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.error,
        )

    fun getEntryColor(index: Int): Color = palette[index % palette.size]

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f),
    ) {
        val sweepAngles = displayData.map { (it.count.toFloat() / total) * 360f }
        val donutSize = 136.dp

        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "All-Time Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontFamily = flexFontRounded(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
            Canvas(
                modifier =
                    Modifier.size(donutSize).pointerInput(displayData, sweepAngles) {
                        detectTapGestures { tapOffset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = tapOffset.x - center.x
                            val dy = tapOffset.y - center.y
                            val angleRad = kotlin.math.atan2(dy, dx)
                            var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                            if (angleDeg < 0) angleDeg += 360f
                            val angleFromTop = (angleDeg - 270f + 360f) % 360f

                            var currentAngle = 0f
                            for (i in sweepAngles.indices) {
                                val sweep = sweepAngles[i]
                                if (
                                    angleFromTop >= currentAngle &&
                                        angleFromTop < currentAngle + sweep
                                ) {
                                    onSelectHabit(displayData[i].id, displayData[i].title)
                                    break
                                }
                                currentAngle += sweep
                            }
                        }
                    }
            ) {
                var startAngle = -90f
                displayData.forEachIndexed { i, entry ->
                    val sweep = sweepAngles[i]
                    drawArc(
                        color = getEntryColor(i),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height),
                        style = Stroke(width = 32f),
                    )
                    startAngle += sweep
                }
            }

            Column(
                modifier = Modifier.weight(1f).padding(start = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                displayData.forEachIndexed { i, entry ->
                    val pct = ((sweepAngles[i] / 360f) * 100f).toInt()
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectHabit(entry.id, entry.title) }
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier.size(10.dp)
                                    .background(color = getEntryColor(i), shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = flexFontRounded(),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "$pct%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = flexFontRounded(),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
}
