package com.loc.hexis.shared.ui.habit.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions.Companion.Default
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.habits.TimeDivision
import com.loc.hexis.shared.ui.components.HexisBottomSheet
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import hexis.shared.ui.generated.resources.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.vectorResource

@Composable
fun TimeDivisionUpsertSheet(
    isEditSheet: Boolean = false,
    modifier: Modifier = Modifier,
    division: TimeDivision,
    onDismiss: () -> Unit,
    onUpsert: (TimeDivision) -> Unit,
) {
    var newDivision by remember { mutableStateOf(division) }

    val textFieldState =
        rememberTextFieldState(
            initialText = newDivision.name,
            initialSelection = TextRange(newDivision.name.length),
        )

    HexisBottomSheet(
        modifier = modifier.imePadding(),
        padding = 0.dp,
        onDismissRequest = onDismiss,
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            delay(400)
            focusRequester.requestFocus()
            keyboardController?.show()
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
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
                    imageVector =
                        vectorResource(if (isEditSheet) Res.drawable.edit else Res.drawable.add),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                text = if (isEditSheet) "Edit Time Division" else "Add Time Division",
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = flexFontEmphasis()),
            )

            OutlinedTextField(
                state = textFieldState,
                lineLimits = TextFieldLineLimits.SingleLine,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions =
                    Default.copy(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                placeholder = { Text(text = "Name") },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )

            Button(
                onClick = { onUpsert(newDivision.copy(name = textFieldState.text.toString())) },
                shapes =
                    ButtonShapes(
                        shape = MaterialTheme.shapes.extraLarge,
                        pressedShape = MaterialTheme.shapes.small,
                    ),
                modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth(),
                enabled = textFieldState.text.isNotBlank() && textFieldState.text.length <= 20,
            ) {
                Text(text = if (isEditSheet) "Done" else "Save")
            }
        }
    }
}
