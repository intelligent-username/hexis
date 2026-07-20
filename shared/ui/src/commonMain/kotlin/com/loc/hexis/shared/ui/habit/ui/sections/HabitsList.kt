package com.loc.hexis.shared.ui.habit.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.now
import com.loc.hexis.shared.ui.LocalWindowSizeClass
import com.loc.hexis.shared.ui.components.Empty
import com.loc.hexis.shared.ui.components.detachedItemShape
import com.loc.hexis.shared.ui.components.endItemShape
import com.loc.hexis.shared.ui.components.leadingItemShape
import com.loc.hexis.shared.ui.components.middleItemShape
import com.loc.hexis.shared.ui.habit.HabitState
import com.loc.hexis.shared.ui.habit.HabitsAction
import com.loc.hexis.shared.ui.habit.ui.component.HabitCard
import com.loc.hexis.shared.ui.habit.ui.component.HabitUpsertSheet
import com.loc.hexis.shared.ui.habit.ui.component.TimeDivisionEditDialog
import hexis.shared.ui.generated.resources.*
import kotlin.time.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.vectorResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun HabitsList(
    state: HabitState,
    lazyListState: LazyListState,
    onAction: (HabitsAction) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val reorderableListState =
        rememberReorderableLazyListState(lazyListState) { from, to ->
            onAction(HabitsAction.OnTransientHabitReorder(from.index, to.index))
        }

    Column(modifier = modifier) {
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxHeight(),
        ) {
            // habits
            val displayedHabits =
                if (state.reorderHabits)
                    state.habitsWithAnalytics.sortedBy {
                        it.habit.id in state.completedHabitIds
                    }
                else state.habitsWithAnalytics
            itemsIndexed(displayedHabits, key = { _, it -> it.habit.id }) {
                index,
                habitWithAnalytics ->
                ReorderableItem(reorderableListState, key = habitWithAnalytics.habit.id) {
                    val completed = state.completedHabitIds.contains(habitWithAnalytics.habit.id)
                    val completedIndices =
                        displayedHabits.indices.filter {
                            displayedHabits[it].habit.id in state.completedHabitIds
                        }
                    val shape =
                        when {
                            !completed || completedIndices.size == 1 ->
                                detachedItemShape(radius = 28)
                            index == completedIndices.first() ->
                                leadingItemShape(topRadius = 28, bottomRadius = 8)
                            index == completedIndices.last() ->
                                endItemShape(bottomRadius = 28, topRadius = 8)
                            else -> middleItemShape(radius = 8)
                        }

                    HabitCard(
                        habitWithAnalytics = habitWithAnalytics,
                        completed = completed,
                        action = onAction,
                        startingDay = state.startingDay,
                        editState = state.editState,
                        onNavigateToAnalytics = onNavigateToAnalytics,
                        is24Hr = state.is24Hr,
                        reorderHandle = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.drag_indicator),
                                contentDescription = "Drag Indicator",
                                modifier =
                                    Modifier.draggableHandle(
                                        onDragStopped = { onAction(HabitsAction.ReorderHabits) }
                                    ),
                            )
                        },
                        shape = shape,
                        compactView = state.compactHabitView,
                        analyticsEnabled =
                            state.analyticsHabitId != habitWithAnalytics.habit.id ||
                                windowSizeClass.widthSizeClass != WindowWidthSizeClass.Expanded,
                    )
                }
            }

            // when no habits
            if (state.habitsWithAnalytics.isEmpty()) {
                item {
                    Empty(
                        modifier = Modifier.padding(top = 150.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }

    // add dialog
    val showTimeDivisionEditDialog = remember { mutableStateOf(false) }

    if (showTimeDivisionEditDialog.value) {
        TimeDivisionEditDialog(
            state = state,
            onAction = onAction,
            onDismiss = { showTimeDivisionEditDialog.value = false },
        )
    }

    if (state.showHabitAddSheet) {
        val newHabitId = requireNotNull(state.newHabitId)
        HabitUpsertSheet(
            habit =
                Habit(
                    id = newHabitId,
                    title = "",
                    description = "",
                    time = LocalDateTime.now(),
                    days = DayOfWeek.entries.toSet(),
                    index = state.habitsWithAnalytics.size,
                    reminder = false,
                ),
            timeDivisions = state.timeDivisions,
            selectedDivisionId = state.selectedTimeDivisionId,
            onDismissRequest = { onAction(HabitsAction.DismissAddHabitDialog) },
            onUpsertHabit = { habit, divId ->
                onAction(HabitsAction.AddHabitWithDivision(habit, divId))
            },
            onManageTimeDivisions = { showTimeDivisionEditDialog.value = true },
            is24Hr = state.is24Hr,
        )
    }
}
