package com.loc.hexis.shared.ui.habit.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.minusMonths
import com.loc.hexis.core.toFormattedString
import com.loc.hexis.shared.ui.LocalWindowSizeClass
import com.loc.hexis.shared.ui.components.HexisDialog
import com.loc.hexis.shared.ui.habit.HabitState
import com.loc.hexis.shared.ui.habit.HabitsAction
import com.loc.hexis.shared.ui.habit.ui.component.HabitUpsertSheet
import com.loc.hexis.shared.ui.habit.ui.component.TimeDivisionEditDialog
import com.loc.hexis.shared.ui.habit.ui.component.stats.CalendarMap
import com.loc.hexis.shared.ui.habit.ui.component.stats.PointsStatCards
import com.loc.hexis.shared.ui.habit.ui.component.stats.StartStats
import com.loc.hexis.shared.ui.habit.ui.component.stats.TrendLineChart
import com.loc.hexis.shared.ui.habit.ui.component.stats.WeekDayBreakdown
import com.loc.hexis.shared.ui.habit.ui.component.stats.WeeklyActivity
import com.loc.hexis.shared.ui.habit.ui.component.stats.WeeklyBooleanHeatMap
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import com.loc.hexis.shared.ui.util.rememberToday
import hexis.shared.ui.generated.resources.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun AnalyticsPage(
    state: HabitState,
    onAction: (HabitsAction) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onPomodoroClick: (Long?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalWindowSizeClass.current

    val today by rememberToday()
    var selectedDate by remember(today) { mutableStateOf(today) }
    val currentMonth = today.yearMonth
    val currentHabit =
        state.habitsWithAnalytics.find { it.habit.id == state.analyticsHabitId } ?: return

    val heatMapState =
        rememberHeatMapCalendarState(
            startMonth = currentMonth.minusMonths(12),
            endMonth = currentMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = state.startingDay,
        )
    val calendarState =
        rememberCalendarState(
            startMonth = currentMonth.minusMonths(12),
            endMonth = currentMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = state.startingDay,
        )

    var editDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Column(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize()) {
        MediumFlexibleTopAppBar(
            scrollBehavior = scrollBehavior,
            colors =
                TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = Color.Transparent,
                    containerColor = Color.Transparent,
                ),
            title = { Text(text = currentHabit.habit.title, maxLines = 1, fontFamily = flexFontEmphasis()) },
            subtitle = {
                if (currentHabit.habit.description.isNotEmpty()) {
                    Text(
                        text = currentHabit.habit.description,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(),
                        fontFamily = flexFontRounded(),
                    )
                }
            },
            windowInsets =
                if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
                    WindowInsets(0)
                } else {
                    TopAppBarDefaults.windowInsets
                },
            navigationIcon = {
                FilledTonalIconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.nav_arrow_back),
                        contentDescription = "Navigate Back",
                    )
                }
            },
            actions = {
                val isArchived = currentHabit.habit.id in state.archivedHabitIds
                OutlinedIconButton(
                    onClick = {
                        onAction(
                            HabitsAction.ToggleArchiveHabit(currentHabit.habit.id, !isArchived)
                        )
                    },
                    shapes =
                        IconButtonShapes(
                            shape = CircleShape,
                            pressedShape = MaterialTheme.shapes.small,
                        ),
                ) {
                    Icon(
                        imageVector =
                            vectorResource(
                                if (isArchived) Res.drawable.unarchive else Res.drawable.archive
                            ),
                        contentDescription = if (isArchived) "Unarchive Habit" else "Archive Habit",
                    )
                }

                OutlinedIconButton(
                    onClick = { deleteDialog = true },
                    shapes =
                        IconButtonShapes(
                            shape = CircleShape,
                            pressedShape = MaterialTheme.shapes.small,
                        ),
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.delete),
                        contentDescription = "Delete Habit",
                    )
                }

                if (
                    currentHabit.habit.pomodoroLinked &&
                        currentHabit.habit.displayMode ==
                            com.loc.hexis.core.habits.DisplayMode.PROGRESS
                ) {
                    FilledTonalIconButton(
                        onClick = { onPomodoroClick(currentHabit.habit.id) },
                        shapes =
                            IconButtonShapes(
                                shape = CircleShape,
                                pressedShape = MaterialTheme.shapes.small,
                            ),
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.schedule),
                            contentDescription = "Go to Pomodoro",
                        )
                    }
                }

                FilledIconButton(
                    onClick = { editDialog = true },
                    shapes =
                        IconButtonShapes(
                            shape = CircleShape,
                            pressedShape = MaterialTheme.shapes.small,
                        ),
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.edit),
                        contentDescription = "Edit Habit",
                    )
                }
            },
        )

        val maxWidth = 380.dp
        LazyVerticalGrid(
            modifier =
                Modifier.fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            columns = GridCells.Adaptive(minSize = maxWidth),
            contentPadding = PaddingValues(top = 16.dp, bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                StartStats(
                    consistency = currentHabit.consistency,
                    startDate = currentHabit.habit.time.date,
                    bestStreak = currentHabit.bestStreak,
                    currentStreak = currentHabit.currentStreak,
                )
            }

            item {
                PointsStatCards(
                    pointsSummary = currentHabit.pointsSummary,
                    modifier = Modifier.widthIn(max = maxWidth),
                )
            }

            item {
                TrendLineChart(
                    weeklyPointsHistory = currentHabit.pointsSummary.weeklyPointsHistory,
                    modifier = Modifier.widthIn(max = maxWidth),
                )
            }

            if (currentHabit.habit.displayMode == com.loc.hexis.core.habits.DisplayMode.PROGRESS) {
                val currentValue = currentHabit.statuses.find { it.date == selectedDate }?.value ?: 0.0
                val targetValue = currentHabit.habit.targetValue ?: 1.0
                val incrementBy = currentHabit.habit.incrementBy
                val progressFraction = if (targetValue > 0.0) (currentValue / targetValue).coerceIn(0.0, 1.0).toFloat() else 0f
                val isTargetReached = currentValue >= (targetValue - 0.001)

                item {
                    val dateDiff = selectedDate.toEpochDays() - today.toEpochDays()
                    val dateTitle = when (dateDiff) {
                        0L -> "Today's Progress"
                        -1L -> "Yesterday's Progress"
                        else -> "${selectedDate.toFormattedString()} Progress"
                    }

                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth().widthIn(max = maxWidth),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // Date Navigation Bar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        selectedDate = selectedDate.minus(1, DateTimeUnit.DAY)
                                    },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = vectorResource(Res.drawable.arrow_back),
                                        contentDescription = "Previous Day",
                                        modifier = Modifier.size(18.dp),
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dateTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = flexFontEmphasis(),
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )

                                    if (selectedDate != today) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                                                selectedDate = today
                                            },
                                        ) {
                                            Text(
                                                text = "Reset to Today",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = flexFontRounded(),
                                                    fontSize = 11.sp,
                                                ),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                }

                                FilledTonalIconButton(
                                    onClick = {
                                        if (selectedDate < today) {
                                            selectedDate = selectedDate.plus(1, DateTimeUnit.DAY)
                                        }
                                    },
                                    enabled = selectedDate < today,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = vectorResource(Res.drawable.arrow_forward),
                                        contentDescription = "Next Day",
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Counter Controls & Value
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        if (currentValue > 0.0) {
                                            onAction(
                                                HabitsAction.DecrementHabitProgress(
                                                    currentHabit.habit,
                                                    selectedDate,
                                                )
                                            )
                                        }
                                    },
                                    enabled = currentValue > 0.0,
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Text(
                                        text = "−",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontFamily = flexFontRounded(),
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = if (currentValue % 1.0 == 0.0) currentValue.toLong().toString() else currentValue.toString(),
                                            style = MaterialTheme.typography.displayMedium.copy(
                                                fontFamily = flexFontRounded(),
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = if (isTargetReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = " / ${if (targetValue % 1.0 == 0.0) targetValue.toLong().toString() else targetValue.toString()}",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontFamily = flexFontRounded(),
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 6.dp),
                                        )
                                    }

                                    if (isTargetReached) {
                                        Text(
                                            text = "Target Met",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = flexFontRounded(),
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    } else if (incrementBy != 1.0) {
                                        Text(
                                            text = "Step: ±${if (incrementBy % 1.0 == 0.0) incrementBy.toLong().toString() else incrementBy.toString()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        )
                                    }
                                }

                                FilledIconButton(
                                    onClick = {
                                        onAction(
                                            HabitsAction.IncrementHabitProgress(
                                                currentHabit.habit,
                                                selectedDate,
                                            )
                                        )
                                    },
                                    enabled = selectedDate <= today,
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(
                                        imageVector = vectorResource(Res.drawable.add),
                                        contentDescription = "Increment",
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Animated Linear Progress Bar
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            )
                        }
                    }
                }
            }

            item {
                WeeklyBooleanHeatMap(
                    heatMapState = heatMapState,
                    statuses = currentHabit.statuses,
                    targetValue = currentHabit.habit.targetValue ?: 1.0,
                    displayMode = currentHabit.habit.displayMode,
                    days = currentHabit.habit.days,
                    startDate = currentHabit.habit.time.date,
                    onDateClick = {
                        selectedDate = it
                        if (currentHabit.habit.displayMode != com.loc.hexis.core.habits.DisplayMode.PROGRESS) {
                            onAction(HabitsAction.ToggleHabitProgress(currentHabit.habit, it))
                        }
                    },
                )
            }

            item {
                CalendarMap(
                    calendarState = calendarState,
                    statuses = currentHabit.statuses,
                    targetValue = currentHabit.habit.targetValue ?: 1.0,
                    displayMode = currentHabit.habit.displayMode,
                    days = currentHabit.habit.days,
                    startDate = currentHabit.habit.time.date,
                    onNavigateToCalendar = onNavigateToCalendar,
                    onDateClick = {
                        selectedDate = it
                        if (currentHabit.habit.displayMode != com.loc.hexis.core.habits.DisplayMode.PROGRESS) {
                            onAction(
                                HabitsAction.ToggleHabitProgress(habit = currentHabit.habit, date = it)
                            )
                        }
                    },
                )
            }

            item {
                WeeklyActivity(
                    lineChartData = currentHabit.weeklyComparisonData,
                    modifier = Modifier.widthIn(max = maxWidth),
                )
            }

            item {
                WeekDayBreakdown(
                    weekDayData = currentHabit.weekDayFrequencyData,
                    modifier = Modifier.widthIn(max = maxWidth),
                )
            }
        }
    }

    // delete dialog
    if (deleteDialog) {
        HexisDialog(onDismissRequest = { deleteDialog = false }) {
            Column {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier.size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialShapes.Pill.toShape(),
                            ),
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.warning),
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.delete),
                    style =
                        MaterialTheme.typography.headlineSmall.copy(fontFamily = flexFontEmphasis()),
                )
                Text(
                    text = stringResource(Res.string.delete_warning),
                    style = MaterialTheme.typography.bodyLarge,
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = { deleteDialog = false },
                        shapes =
                            ButtonShapes(
                                shape = MaterialTheme.shapes.extraLarge,
                                pressedShape = MaterialTheme.shapes.small,
                            ),
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }

                    TextButton(
                        onClick = {
                            onAction(HabitsAction.DeleteHabit(currentHabit.habit))
                            onAction(HabitsAction.PrepareAnalytics(null))
                            deleteDialog = false
                            onNavigateBack()
                        },
                        shapes =
                            ButtonShapes(
                                shape = MaterialTheme.shapes.extraLarge,
                                pressedShape = MaterialTheme.shapes.small,
                            ),
                    ) {
                        Text(stringResource(Res.string.delete))
                    }
                }
            }
        }
    }

    val showTimeDivisionEditDialog = remember { mutableStateOf(false) }

    if (showTimeDivisionEditDialog.value) {
        TimeDivisionEditDialog(
            state = state,
            onAction = onAction,
            onDismiss = { showTimeDivisionEditDialog.value = false },
        )
    }

    if (editDialog) {
        HabitUpsertSheet(
            habit = currentHabit.habit,
            timeDivisions = state.timeDivisions,
            selectedDivisionId = state.habitTimeDivisionMap[currentHabit.habit.id],
            onDismissRequest = { editDialog = false },
            onUpsertHabit = { habit, divId ->
                onAction(HabitsAction.UpdateHabit(habit))
                onAction(HabitsAction.SetHabitTimeDivision(habit.id, divId))
            },
            onManageTimeDivisions = { showTimeDivisionEditDialog.value = true },
            is24Hr = state.is24Hr,
            isEditSheet = true,
        )
    }
}
