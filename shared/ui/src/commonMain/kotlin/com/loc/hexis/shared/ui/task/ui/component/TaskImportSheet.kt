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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
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
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.tasks.Category
import com.loc.hexis.core.tasks.Task
import com.loc.hexis.core.tasks.TaskImportParser
import com.loc.hexis.shared.ui.components.HexisBottomSheet
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import hexis.shared.ui.generated.resources.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun TaskImportSheet(
    categories: List<Category>,
    currentCategory: Category?,
    onDismissRequest: () -> Unit,
    onImport: (List<Task>, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    var selectedCategory by
        remember(categories, currentCategory) {
            mutableStateOf(currentCategory ?: categories.firstOrNull())
        }

    val parsedItems = remember(text) { TaskImportParser.parse(text) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(400)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    HexisBottomSheet(
        modifier = modifier.imePadding(),
        padding = 0.dp,
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    imageVector = vectorResource(Res.drawable.download),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = stringResource(Res.string.import_tasks),
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = flexFontEmphasis()
                    ),
            )

            Text(
                text = stringResource(Res.string.import_tasks_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (categories.size > 1) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(Res.string.import_to_category),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        categories.forEach { category ->
                            ToggleButton(
                                checked = category.id == selectedCategory?.id,
                                onCheckedChange = { selectedCategory = category },
                                colors = ToggleButtonDefaults.tonalToggleButtonColors(),
                                content = { Text(category.name) },
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                shape = MaterialTheme.shapes.medium,
                placeholder = {
                    Text(
                        text =
                            "- Homework 1\n> Due September 15th, worth 15%\n>Will cover prereqs and W1\n\n- Homework 2\n> Due September 16th",
                        color = MaterialTheme.colorScheme.outline,
                    )
                },
                modifier =
                    Modifier.fillMaxWidth()
                        .heightIn(min = 140.dp, max = 240.dp)
                        .focusRequester(focusRequester),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onDismissRequest,
                    shapes =
                        ButtonShapes(
                            shape = MaterialTheme.shapes.extraLarge,
                            pressedShape = MaterialTheme.shapes.small,
                        ),
                ) {
                    Text(stringResource(Res.string.cancel))
                }

                Button(
                    onClick = {
                        val targetCategory = selectedCategory ?: categories.firstOrNull()
                        if (targetCategory != null && parsedItems.isNotEmpty()) {
                            val tasksToImport =
                                parsedItems.map { item ->
                                    Task(
                                        categoryId = targetCategory.id,
                                        title = item.title,
                                        description = item.description,
                                    )
                                }
                            onImport(tasksToImport, targetCategory.id)
                            onDismissRequest()
                        }
                    },
                    enabled = parsedItems.isNotEmpty() && (selectedCategory != null || categories.isNotEmpty()),
                    shapes =
                        ButtonShapes(
                            shape = MaterialTheme.shapes.extraLarge,
                            pressedShape = MaterialTheme.shapes.small,
                        ),
                ) {
                    Text(
                        text =
                            if (parsedItems.isNotEmpty()) {
                                "${stringResource(Res.string.import_label)} (${parsedItems.size})"
                            } else {
                                stringResource(Res.string.import_label)
                            }
                    )
                }
            }
        }
    }
}
