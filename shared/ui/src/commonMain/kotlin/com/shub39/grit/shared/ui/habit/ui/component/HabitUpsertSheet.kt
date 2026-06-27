package com.shub39.grit.shared.ui.habit.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.toShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.habits.DisplayMode
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.TimeDivision
import com.shub39.grit.core.now
import com.shub39.grit.core.toFormattedString
import com.shub39.grit.shared.ui.components.ExpressiveSwitch
import com.shub39.grit.shared.ui.components.HexisBottomSheet
import com.shub39.grit.shared.ui.components.HexisTimePicker
import com.shub39.grit.shared.ui.components.detachedItemShape
import com.shub39.grit.shared.ui.components.endItemShape
import com.shub39.grit.shared.ui.components.leadingItemShape
import com.shub39.grit.shared.ui.components.listItemColors
import com.shub39.grit.shared.ui.components.middleItemShape
import com.shub39.grit.shared.ui.theme.HexisTheme
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import com.shub39.grit.shared.ui.theme.flexFontRounded
import grit.shared.ui.generated.resources.*
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
expect fun HabitUpsertSheet(
    habit: Habit,
    timeDivisions: List<TimeDivision>,
    selectedDivisionId: Long?,
    onDismissRequest: () -> Unit,
    onUpsertHabit: (Habit, Long?) -> Unit,
    onManageTimeDivisions: () -> Unit,
    is24Hr: Boolean,
    modifier: Modifier = Modifier,
    isEditSheet: Boolean = false,
)

@Composable
fun HabitUpsertSheetContent(
    newHabit: Habit,
    timeDivisions: List<TimeDivision>,
    selectedDivisionId: Long?,
    updateHabit: (Habit) -> Unit,
    onDismissRequest: () -> Unit,
    onUpsertHabit: (Habit, Long?) -> Unit,
    onManageTimeDivisions: () -> Unit,
    is24Hr: Boolean,
    isEditSheet: Boolean = false,
    notificationPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var timePickerDialog by remember { mutableStateOf(false) }
    var localSelectedDivisionId by remember { mutableStateOf(selectedDivisionId) }

    val titleTextFieldState =
        rememberTextFieldState(
            initialText = newHabit.title,
            initialSelection = TextRange(newHabit.title.length),
        )

    val descTextFieldState =
        rememberTextFieldState(
            initialText = newHabit.description,
            initialSelection = TextRange(newHabit.description.length),
        )

    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        delay(400)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    HexisBottomSheet(
        onDismissRequest = onDismissRequest,
        padding = 0.dp,
        modifier = modifier
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier.size(50.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialShapes.Pill.toShape(),
                        ),
            ) {
                Icon(
                    imageVector =
                        vectorResource(if (isEditSheet) Res.drawable.edit else Res.drawable.add),
                    contentDescription = "Edit Habit",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text =
                    stringResource(
                        if (isEditSheet) Res.string.edit_habit else Res.string.add_habit
                    ),
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = flexFontEmphasis()),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                OutlinedTextField(
                    state = titleTextFieldState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                    label = {
                        if (titleTextFieldState.text.trimEnd().length <= 20) {
                            Text(
                                text =
                                    stringResource(
                                        if (isEditSheet) Res.string.update_title
                                        else Res.string.title
                                    )
                            )
                        } else {
                            Text(text = stringResource(Res.string.too_long))
                        }
                    },
                    isError = titleTextFieldState.text.trimEnd().length > 20,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )
            }

            item {
                OutlinedTextField(
                    state = descTextFieldState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        if (descTextFieldState.text.trimEnd().length <= 50) {
                            Text(
                                text =
                                    stringResource(
                                        if (isEditSheet) Res.string.update_description
                                        else Res.string.description
                                    )
                            )
                        } else {
                            Text(text = stringResource(Res.string.too_long))
                        }
                    },
                    isError = descTextFieldState.text.trimEnd().length > 50,
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Card(
                        shape =
                            if (newHabit.days.isEmpty()) detachedItemShape()
                            else leadingItemShape(),
                        modifier = Modifier.animateContentSize(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(text = stringResource(Res.string.select_days))

                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                            ) {
                                DayOfWeek.entries.forEach { dayOfWeek ->
                                    ToggleButton(
                                        checked = newHabit.days.contains(dayOfWeek),
                                        onCheckedChange = {
                                            updateHabit(
                                                newHabit.copy(
                                                    days =
                                                        if (it) {
                                                            newHabit.days + dayOfWeek
                                                        } else {
                                                            newHabit.days - dayOfWeek
                                                        }
                                                )
                                            )
                                        },
                                        enabled =
                                            !(newHabit.days.size == 1 &&
                                                newHabit.days.contains(dayOfWeek)),
                                        modifier = Modifier.weight(1f),
                                        colors = ToggleButtonDefaults.tonalToggleButtonColors(),
                                        content = { Text(text = dayOfWeek.name.take(1)) },
                                    )
                                }
                            }
                        }
                    }

                    if (newHabit.days.isNotEmpty()) {
                        ListItem(
                            colors = listItemColors(),
                            modifier =
                                Modifier.clip(
                                    if (newHabit.reminder) middleItemShape() else endItemShape()
                                ),
                            headlineContent = {
                                Text(text = stringResource(Res.string.add_reminder))
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(Res.string.add_reminder_desc),
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee(),
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.alarm),
                                    contentDescription = "Alarm Icon",
                                )
                            },
                            trailingContent = {
                                ExpressiveSwitch(
                                    checked = newHabit.reminder,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            if (notificationPermission) {
                                                updateHabit(newHabit.copy(reminder = true))
                                            } else {
                                                onRequestPermission()
                                            }
                                        } else {
                                            updateHabit(newHabit.copy(reminder = false))
                                        }
                                    },
                                )
                            },
                        )

                        if (newHabit.reminder) {
                            ListItem(
                                colors = listItemColors(),
                                modifier = Modifier.clip(endItemShape()),
                                headlineContent = {
                                    Text(
                                        text =
                                            newHabit.time.time.toFormattedString(is24Hr = is24Hr),
                                        style =
                                            MaterialTheme.typography.titleLarge.copy(
                                                fontFamily = flexFontRounded()
                                            ),
                                    )
                                },
                                trailingContent = {
                                    FilledTonalIconButton(onClick = { timePickerDialog = true }) {
                                        Icon(
                                            imageVector = vectorResource(Res.drawable.edit),
                                            contentDescription = "Pick Time",
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Display Mode",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                        ) {
                            ToggleButton(
                                checked = newHabit.displayMode == DisplayMode.CHECKBOX,
                                onCheckedChange = {
                                    updateHabit(newHabit.copy(displayMode = DisplayMode.CHECKBOX))
                                },
                                colors = ToggleButtonDefaults.tonalToggleButtonColors(),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(text = "Checkbox")
                            }
                            ToggleButton(
                                checked = newHabit.displayMode == DisplayMode.PROGRESS,
                                onCheckedChange = {
                                    updateHabit(newHabit.copy(displayMode = DisplayMode.PROGRESS))
                                },
                                colors = ToggleButtonDefaults.tonalToggleButtonColors(),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(text = "Progress")
                            }
                        }
                        if (newHabit.displayMode == DisplayMode.PROGRESS) {
                            Spacer(modifier = Modifier.height(8.dp))
                            var targetValueText by remember(newHabit.targetValue) {
                                mutableStateOf(newHabit.targetValue?.toInt()?.toString() ?: "")
                            }
                            OutlinedTextField(
                                value = targetValueText,
                                onValueChange = { value ->
                                    if (value.isEmpty() || value.all { it.isDigit() }) {
                                        targetValueText = value
                                        val num = value.toIntOrNull()
                                        if (num != null && num > 0) {
                                            updateHabit(newHabit.copy(targetValue = num.toDouble()))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Target Value") },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ListItem(
                                colors = listItemColors(),
                                headlineContent = {
                                    Text(text = "Pomodoro Linked")
                                },
                                supportingContent = {
                                    Text(
                                        text = "Auto-increment on focus complete",
                                        maxLines = 1,
                                        modifier = Modifier.basicMarquee(),
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = vectorResource(Res.drawable.schedule),
                                        contentDescription = "Pomodoro",
                                    )
                                },
                                trailingContent = {
                                    ExpressiveSwitch(
                                        checked = newHabit.pomodoroLinked,
                                        onCheckedChange = { checked ->
                                            updateHabit(newHabit.copy(pomodoroLinked = checked))
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Time Division",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f),
                            )
                            FilledTonalIconButton(onClick = onManageTimeDivisions) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.edit),
                                    contentDescription = "Manage Divisions",
                                )
                            }
                        }

                        if (timeDivisions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp),
                            ) {
                                ToggleButton(
                                    checked = localSelectedDivisionId == null,
                                    onCheckedChange = { localSelectedDivisionId = null },
                                    colors = ToggleButtonDefaults.tonalToggleButtonColors(),
                                ) {
                                    Text(text = "None")
                                }
                                timeDivisions.forEach { div ->
                                    ToggleButton(
                                        checked = localSelectedDivisionId == div.id,
                                        onCheckedChange = { localSelectedDivisionId = div.id },
                                        colors = ToggleButtonDefaults.tonalToggleButtonColors(),
                                    ) {
                                        Text(text = div.name)
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No time divisions yet. Tap edit to create one.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        onUpsertHabit(
                            newHabit.copy(
                                title = titleTextFieldState.text.toString().trim(),
                                description = descTextFieldState.text.toString().trim(),
                            ),
                            localSelectedDivisionId,
                        )
                        onDismissRequest()
                    },
                    modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth(),
                    enabled =
                        descTextFieldState.text.trimEnd().length <= 50 &&
                            titleTextFieldState.text.trimEnd().length <= 20 &&
                            titleTextFieldState.text.isNotBlank(),
                ) {
                    Text(
                        text =
                            stringResource(
                                if (isEditSheet) {
                                    Res.string.save
                                } else {
                                    Res.string.add_habit
                                }
                            )
                    )
                }
            }
        }

        if (timePickerDialog) {
            val timePickerState =
                rememberTimePickerState(
                    initialHour = newHabit.time.hour,
                    initialMinute = newHabit.time.minute,
                    is24Hour = is24Hr,
                )

            HexisTimePicker(
                onDismissRequest = { timePickerDialog = false },
                state = timePickerState,
                onConfirm = {
                    updateHabit(
                        newHabit.copy(
                            time =
                                LocalDateTime(
                                    date = newHabit.time.date,
                                    time =
                                        LocalTime(
                                            minute = timePickerState.minute,
                                            hour = timePickerState.hour,
                                        ),
                                )
                        )
                    )
                    timePickerDialog = false
                },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    HexisTheme {
        HabitUpsertSheet(
            habit =
                Habit(
                    id = 1,
                    title = "New Habit",
                    description = "A new Habit",
                    time = LocalDateTime.now(),
                    days = DayOfWeek.entries.toSet(),
                    index = 1,
                    reminder = false,
                ),
            timeDivisions = emptyList(),
            selectedDivisionId = null,
            onDismissRequest = {},
            onUpsertHabit = { _, _ -> },
            onManageTimeDivisions = {},
            is24Hr = true,
            isEditSheet = true,
        )
    }
}
