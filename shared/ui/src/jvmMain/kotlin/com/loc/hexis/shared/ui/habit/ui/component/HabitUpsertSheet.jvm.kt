package com.loc.hexis.shared.ui.habit.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.loc.hexis.core.habits.Habit
import com.loc.hexis.core.habits.TimeDivision

@Composable
actual fun HabitUpsertSheet(
    habit: Habit,
    timeDivisions: List<TimeDivision>,
    selectedDivisionId: Long?,
    onDismissRequest: () -> Unit,
    onUpsertHabit: (Habit, Long?) -> Unit,
    onManageTimeDivisions: () -> Unit,
    is24Hr: Boolean,
    modifier: Modifier,
    isEditSheet: Boolean,
) {
    var newHabit by remember { mutableStateOf(habit) }

    HabitUpsertSheetContent(
        newHabit = newHabit,
        timeDivisions = timeDivisions,
        selectedDivisionId = selectedDivisionId,
        updateHabit = { newHabit = it },
        onDismissRequest = onDismissRequest,
        onUpsertHabit = onUpsertHabit,
        onManageTimeDivisions = onManageTimeDivisions,
        is24Hr = is24Hr,
        isEditSheet = isEditSheet,
        notificationPermission = true,
        onRequestPermission = {},
        modifier = modifier,
    )
}
