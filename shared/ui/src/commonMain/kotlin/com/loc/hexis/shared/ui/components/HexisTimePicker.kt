package com.loc.hexis.shared.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import hexis.shared.ui.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun HexisTimePicker(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    state: TimePickerState = rememberTimePickerState(),
    onConfirm: () -> Unit,
) {
    var timeInput by remember { mutableStateOf(false) }

    TimePickerDialog(
        modifier = modifier.animateContentSize(animationSpec = tween(0)),
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(Res.string.select_time)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(Res.string.done)) }
        },
        modeToggleButton = {
            IconButton(onClick = { timeInput = !timeInput }) {
                Icon(
                    imageVector =
                        vectorResource(
                            if (!timeInput) Res.drawable.keyboard else Res.drawable.clock
                        ),
                    contentDescription = null,
                )
            }
        },
    ) {
        if (timeInput) {
            TimeInput(state = state)
        } else {
            TimePicker(state = state)
        }
    }
}
