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
import androidx.compose.foundation.layout.width
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
import com.loc.hexis.core.habits.countBestStreak
import com.loc.hexis.core.habits.countCurrentStreak
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

import com.loc.hexis.core.interfaces.SettingsDatastore
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroAnalytics(
    onDismiss: () -> Unit,
    onSelectHabit: (Long?, String) -> Unit = { _, _ -> },
) {
    val repo: PomodoroRepo = koinInject()

    val settingsDatastore: SettingsDatastore = koinInject()
    val showPieChart by settingsDatastore.getShowPomodoroPieChartPref().collectAsState(initial = true)

    val dayCounts by repo.getSessionCountsByDay().collectAsState(initial = emptyList())
    val dayMinutes by repo.getSessionMinutesByDay().collectAsState(initial = emptyList())
    val dayMinutesMap = remember(dayMinutes) { dayMinutes.associateBy { it.date } }
    val maxMinutes = remember(dayMinutes) { dayMinutes.maxOfOrNull { it.totalMinutes } ?: 0f }
    val dates = remember(dayCounts) { dayCounts.map { it.date } }

    val totalSessions = dayCounts.sumOf { it.count }
    val totalMinutes = remember(dayMinutes) { dayMinutes.sumOf { it.totalMinutes.toDouble() }.toFloat() }
    val currentStreak = remember(dates) { countCurrentStreak(dates) }
    val bestStreak = remember(dates) { countBestStreak(dates) }

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
                    totalMinutes = totalMinutes,
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                )

                ThisWeekRow(dayMinutes = dayMinutes)

                if (showPieChart) {
                    HabitBreakdownChart(
                        onSelectHabit = { habitId, title ->
                            onSelectHabit(habitId, title)
                            onDismiss()
                        }
                    )
                }

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
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = flexFontRounded(),
                        ),
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
    val formattedTime = if (totalHours > 0) "${totalHours}h ${remMins}m" else "${totalMinutes.toInt()}m"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatPill(value = "$totalSessions", label = "sessions", modifier = Modifier.weight(1f))
            StatPill(value = formattedTime, label = "time worked", modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
private fun ThisWeekRow(dayMinutes: List<PomodoroDayCount>) {
    val today = LocalDate.now()
    val startOfWeek =
        today.minus(today.dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber, DateTimeUnit.DAY)
    val weekMinutes = dayMinutes.filter { it.date in startOfWeek..today }.sumOf { it.totalMinutes.toDouble() }
    val whole = weekMinutes.toInt()

    Text(
        text = "This week: ${whole}m",
        style = MaterialTheme.typography.titleSmall,
        fontFamily = flexFontRounded(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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

    val miscColor = MaterialTheme.colorScheme.outline
    val basePalette =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.error,
            Color(0xFFFF9800),
            Color(0xFF00BCD4),
            Color(0xFF4CAF50),
            Color(0xFF9C27B0),
            Color(0xFFE91E63),
            Color(0xFF3F51B5),
            Color(0xFFFFC107),
            Color(0xFF009688),
        )

    val nonMiscEntries = displayData.filter { it.id != null && !it.title.equals("Misc", ignoreCase = true) }

    fun getEntryColor(entry: HabitCount): Color {
        if (entry.id == null || entry.title.equals("Misc", ignoreCase = true)) {
            return miscColor
        }
        val idx = nonMiscEntries.indexOf(entry).coerceAtLeast(0)
        return if (idx < basePalette.size) {
            basePalette[idx]
        } else {
            val hue = (idx * 137.5f) % 360f
            Color.hsl(hue = hue, saturation = 0.75f, lightness = 0.55f)
        }
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f),
    ) {
        val sweepAngles = displayData.map { (it.count.toFloat() / total) * 360f }
        val donutSize = 136.dp

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                                if (angleFromTop >= currentAngle && angleFromTop < currentAngle + sweep) {
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
                        color = getEntryColor(entry),
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
                                    .background(color = getEntryColor(entry), shape = CircleShape)
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
