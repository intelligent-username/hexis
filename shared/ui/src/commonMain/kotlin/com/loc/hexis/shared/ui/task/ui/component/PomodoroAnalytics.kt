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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HeatMapCalendar
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapCalendarState
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.now
import com.loc.hexis.core.habits.HabitRepo
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
fun PomodoroAnalytics(onDismiss: () -> Unit) {
    val repo: PomodoroRepo = koinInject()

    val dayCounts by repo.getSessionCountsByDay().collectAsState(initial = emptyList())
    val dayCountMap = remember(dayCounts) { dayCounts.associateBy { it.date } }
    val maxCount = remember(dayCounts) { dayCounts.maxOfOrNull { it.count } ?: 0 }
    val dates = remember(dayCounts) { dayCounts.map { it.date } }

    val totalSessions = dayCounts.sumOf { it.count }
    val (currentStreak, bestStreak) = remember(dates) { computeStreaks(dates) }

    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }

    val currentMonth = remember { YearMonth.now() }
    val heatMapState: HeatMapCalendarState =
        rememberHeatMapCalendarState(
            startMonth = currentMonth.minusMonths(12),
            endMonth = currentMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = DayOfWeek.MONDAY,
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Session History", fontFamily = flexFontEmphasis()) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.close),
                            contentDescription = "Close",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        scrolledContainerColor = Color.Transparent,
                        containerColor = Color.Transparent,
                    ),
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
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                )

                ThisWeekRow(dayCounts = dayCounts)

                HabitBreakdownChart()

                SessionHeatMap(
                    heatMapState = heatMapState,
                    dayCountMap = dayCountMap,
                    maxCount = maxCount,
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
        val count = dayCountMap[date]?.count ?: 0
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
                    text = "$count session${if (count != 1) "s" else ""} completed",
                    style =
                        MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = flexFontRounded(),
                        ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun StatsRow(totalSessions: Int, currentStreak: Int, bestStreak: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatPill(value = "$totalSessions", label = "total", modifier = Modifier.weight(1f))
        StatPill(value = "$currentStreak", label = "day streak", modifier = Modifier.weight(1f))
        StatPill(value = "$bestStreak", label = "best streak", modifier = Modifier.weight(1f))
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
private fun ThisWeekRow(dayCounts: List<PomodoroDayCount>) {
    val today = LocalDate.now()
    val startOfWeek =
        today.minus(today.dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber, DateTimeUnit.DAY)
    val weekCount = dayCounts.filter { it.date in startOfWeek..today }.sumOf { it.count }

    Text(
        text = "This week: $weekCount session${if (weekCount != 1) "s" else ""}",
        style = MaterialTheme.typography.titleSmall,
        fontFamily = flexFontRounded(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SessionHeatMap(
    heatMapState: HeatMapCalendarState,
    dayCountMap: Map<LocalDate, PomodoroDayCount>,
    maxCount: Int,
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

                val count = dayCountMap[day.date]?.count ?: 0
                val alpha = if (maxCount > 0) (count.toFloat() / maxCount).coerceIn(0f, 1f) else 0f

                val bgColor =
                    when {
                        day.date > today -> Color.Transparent
                        count == 0 -> MaterialTheme.colorScheme.surfaceContainerHighest
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                    }

                val textColor =
                    when {
                        count == 0 -> MaterialTheme.colorScheme.onSurface
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
                    if (count > 0) {
                        Text(
                            text = count.toString(),
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

private fun computeStreaks(dates: List<LocalDate>): Pair<Int, Int> {
    if (dates.isEmpty()) return 0 to 0
    val sorted = dates.sortedDescending()
    val today = LocalDate.now()

    var current = 0
    for (i in 0 until 365) {
        val check = today.minus(i, DateTimeUnit.DAY)
        if (check in sorted) {
            if (i == 0 || current > 0) current++ else break
        } else if (i > 0) {
            break
        }
    }

    var best = 1
    var streak = 1
    for (i in 1 until sorted.size) {
        if (sorted[i - 1].minus(1, DateTimeUnit.DAY) == sorted[i]) {
            streak++
        } else {
            best = maxOf(best, streak)
            streak = 1
        }
    }
    best = maxOf(best, streak)

    return current to best
}

@Composable
private fun HabitBreakdownChart() {
    val repo: PomodoroRepo = koinInject()
    val habitRepo: HabitRepo = koinInject()

    data class HabitCount(val id: Long?, val count: Int, val title: String)

    var displayData by remember { mutableStateOf<List<HabitCount>>(emptyList()) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val counts = repo.getSessionCountsByHabit()
        displayData =
            counts.map { (id, c) ->
                val title =
                    if (id != null) {
                        (habitRepo.getHabitById(id))?.title ?: "Unknown"
                    } else "Misc"
                HabitCount(id, c, title)
            }
    }

    if (displayData.isEmpty()) return

    val total = displayData.sumOf { it.count }.toFloat()

    val colors =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
        )

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Session Source",
                style = MaterialTheme.typography.titleSmall,
                fontFamily = flexFontRounded(),
            )
            Spacer(Modifier.height(12.dp))

            val sweepAngles = displayData.map { (it.count.toFloat() / total) * 360f }
            val donutSize = 120.dp

            Canvas(modifier = Modifier.size(donutSize)) {
                var startAngle = -90f
                displayData.forEachIndexed { i, _ ->
                    val sweep = sweepAngles[i]
                    drawArc(
                        color = colors[i % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height),
                        style = Stroke(width = 28f),
                    )
                    startAngle += sweep
                }
            }

            Spacer(Modifier.height(12.dp))

            displayData.forEachIndexed { i, entry ->
                val pct = ((sweepAngles[i] / 360f) * 100f).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier =
                            Modifier.size(10.dp)
                                .background(color = colors[i % colors.size], shape = CircleShape)
                    )
                    Text(
                        text = "${entry.title} — $pct%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = flexFontRounded(),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
