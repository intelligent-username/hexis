package com.shub39.grit.shared.ui.habit.ui.sections

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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
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
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.now
import com.shub39.grit.shared.ui.LocalWindowSizeClass
import com.shub39.grit.shared.ui.components.HexisDialog
import com.shub39.grit.shared.ui.habit.HabitState
import com.shub39.grit.shared.ui.habit.HabitsAction
import com.shub39.grit.shared.ui.habit.ui.component.HabitUpsertSheet
import com.shub39.grit.shared.ui.habit.ui.component.TimeDivisionEditDialog
import com.shub39.grit.shared.ui.habit.ui.component.stats.CalendarMap
import com.shub39.grit.shared.ui.habit.ui.component.stats.StartStats
import com.shub39.grit.shared.ui.habit.ui.component.stats.WeekDayBreakdown
import com.shub39.grit.shared.ui.habit.ui.component.stats.WeeklyActivity
import com.shub39.grit.shared.ui.habit.ui.component.stats.WeeklyBooleanHeatMap
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import com.shub39.grit.shared.ui.theme.flexFontRounded
import grit.shared.ui.generated.resources.*
import kotlinx.datetime.YearMonth
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

    val currentMonth = remember { YearMonth.now() }
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
            title = { Text(text = currentHabit.habit.title, fontFamily = flexFontEmphasis()) },
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
                    onClick = { onAction(HabitsAction.ToggleArchiveHabit(currentHabit.habit.id, !isArchived)) },
                    shapes =
                        IconButtonShapes(
                            shape = CircleShape,
                            pressedShape = MaterialTheme.shapes.small,
                        ),
                ) {
                    Icon(
                        imageVector = vectorResource(if (isArchived) Res.drawable.drive_folder_upload else Res.drawable.download),
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

                if (currentHabit.habit.pomodoroLinked && currentHabit.habit.displayMode == com.shub39.grit.core.habits.DisplayMode.PROGRESS) {
                    FilledTonalIconButton(
                        onClick = { onPomodoroClick(currentHabit.habit.id) },
                        shapes = IconButtonShapes(shape = CircleShape, pressedShape = MaterialTheme.shapes.small),
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
        LazyVerticalStaggeredGrid(
            modifier =
                Modifier.fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            columns = StaggeredGridCells.Adaptive(minSize = maxWidth),
            contentPadding = PaddingValues(top = 16.dp, bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 16.dp,
        ) {
            item {
                StartStats(
                    consistency = currentHabit.consistency,
                    startDate = currentHabit.habit.time.date,
                    bestStreak = currentHabit.bestStreak,
                    currentStreak = currentHabit.currentStreak,
                )
            }

            if (currentHabit.habit.displayMode == com.shub39.grit.core.habits.DisplayMode.PROGRESS) {
                val today = kotlinx.datetime.LocalDate.now()
                val currentValue = currentHabit.statuses.find { it.date == today }?.value ?: 0.0
                val targetValue = currentHabit.habit.targetValue ?: 1.0
                val incrementBy = currentHabit.habit.incrementBy

                item {
                    var undoBudget by remember { mutableStateOf(0) }

                    Column(
                        modifier = Modifier.fillMaxWidth().background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = MaterialTheme.shapes.large
                        ).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Today's Progress",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            modifier = Modifier.pointerInput(Unit) {
                                detectVerticalDragGestures { _, dragDistance ->
                                    if (dragDistance > 30f && currentValue > 0.0) {
                                        onAction(HabitsAction.DecrementHabitProgress(currentHabit.habit, today))
                                        undoBudget++
                                    } else if (dragDistance < -30f && undoBudget > 0 && currentValue + incrementBy <= targetValue) {
                                        onAction(HabitsAction.IncrementHabitProgress(currentHabit.habit, today))
                                        undoBudget--
                                    }
                                }
                            },
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentValue > 0.0) {
                                        onAction(HabitsAction.DecrementHabitProgress(currentHabit.habit, today))
                                        undoBudget++
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.arrow_back),
                                    contentDescription = "Decrement",
                                )
                            }

                            Text(
                                text = "${currentValue.toInt()}",
                                style = MaterialTheme.typography.displaySmall.copy(fontFamily = flexFontRounded()),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "/${targetValue.toInt()}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = flexFontRounded()),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            val canUndo = undoBudget > 0 && currentValue + incrementBy <= targetValue
                            IconButton(
                                onClick = {
                                    onAction(HabitsAction.IncrementHabitProgress(currentHabit.habit, today))
                                    undoBudget--
                                },
                                enabled = canUndo,
                            ) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.arrow_forward),
                                    contentDescription = "Increment",
                                )
                            }
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
                    onDateClick = { onAction(HabitsAction.ToggleHabitProgress(currentHabit.habit, it)) },
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
                        onAction(HabitsAction.ToggleHabitProgress(habit = currentHabit.habit, date = it))
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
            onDismiss = { showTimeDivisionEditDialog.value = false }
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