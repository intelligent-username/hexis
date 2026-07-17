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

package com.loc.hexis.shared.ui.habit.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.habits.TimeDivision
import com.loc.hexis.shared.ui.components.Empty
import com.loc.hexis.shared.ui.components.HexisDialog
import com.loc.hexis.shared.ui.components.detachedItemShape
import com.loc.hexis.shared.ui.components.endItemShape
import com.loc.hexis.shared.ui.components.leadingItemShape
import com.loc.hexis.shared.ui.components.listItemColors
import com.loc.hexis.shared.ui.components.middleItemShape
import com.loc.hexis.shared.ui.habit.HabitState
import com.loc.hexis.shared.ui.habit.HabitsAction
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import hexis.shared.ui.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun TimeDivisionEditDialog(
    state: HabitState,
    onAction: (HabitsAction) -> Unit,
    onDismiss: () -> Unit,
) {
    HexisDialog(onDismissRequest = onDismiss, padding = 0.dp) {
        var showAddSheet by remember { mutableStateOf(false) }
        val divisions = state.timeDivisions

        val listState = rememberLazyListState()
        val reorderableListState =
            rememberReorderableLazyListState(listState) { from, to ->
                val mutableList = divisions.toMutableList()
                mutableList.add(to.index, mutableList.removeAt(from.index))
                onAction(
                    HabitsAction.ReorderTimeDivisions(
                        mutableList.mapIndexed { index, div -> index to div }
                    )
                )
            }

        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    imageVector = vectorResource(Res.drawable.edit),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = "Time Divisions",
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = flexFontEmphasis()),
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (divisions.isEmpty()) {
                    item {
                        Empty(
                            modifier = Modifier.padding(vertical = 32.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    itemsIndexed(divisions, key = { _, div -> div.id }) { index, division ->
                        var showEditSheet by remember { mutableStateOf(false) }
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        ReorderableItem(reorderableListState, key = division.id) {
                            val shape =
                                when {
                                    divisions.size == 1 -> detachedItemShape()
                                    index == 0 -> leadingItemShape()
                                    index == divisions.size - 1 -> endItemShape()
                                    else -> middleItemShape()
                                }

                            ListItem(
                                modifier = Modifier.clip(shape),
                                colors = listItemColors(),
                                headlineContent = { Text(text = division.name, maxLines = 1) },
                                supportingContent = {
                                    val count =
                                        state.habitTimeDivisionMap.values.count {
                                            it == division.id
                                        }
                                    Text(text = "$count ${stringResource(Res.string.habits)}")
                                },
                                trailingContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { showEditSheet = true }) {
                                            Icon(
                                                imageVector = vectorResource(Res.drawable.edit),
                                                contentDescription = "Edit",
                                            )
                                        }

                                        IconButton(onClick = { showDeleteDialog = true }) {
                                            Icon(
                                                imageVector = vectorResource(Res.drawable.delete),
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                            )
                                        }

                                        AnimatedVisibility(visible = divisions.size > 1) {
                                            Icon(
                                                imageVector =
                                                    vectorResource(Res.drawable.drag_indicator),
                                                contentDescription = null,
                                                modifier =
                                                    Modifier.padding(horizontal = 8.dp)
                                                        .draggableHandle(),
                                            )
                                        }
                                    }
                                },
                            )
                        }

                        if (showDeleteDialog) {
                            HexisDialog(onDismissRequest = { showDeleteDialog = false }) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.warning),
                                    contentDescription = null,
                                )

                                Text(
                                    text = stringResource(Res.string.delete),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium,
                                )

                                Text(
                                    text = "Are you sure you want to delete this division?",
                                    textAlign = TextAlign.Center,
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    TextButton(
                                        onClick = { showDeleteDialog = false },
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
                                            onAction(HabitsAction.DeleteTimeDivision(division.id))
                                            showDeleteDialog = false
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

                        if (showEditSheet) {
                            TimeDivisionUpsertSheet(
                                isEditSheet = true,
                                division = division,
                                onDismiss = { showEditSheet = false },
                                onUpsert = {
                                    onAction(HabitsAction.UpdateTimeDivision(it))
                                    showEditSheet = false
                                },
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { showAddSheet = true },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shapes =
                    ButtonShapes(
                        shape = MaterialTheme.shapes.extraLarge,
                        pressedShape = MaterialTheme.shapes.small,
                    ),
            ) {
                Text(text = stringResource(Res.string.add))
            }
        }

        if (showAddSheet) {
            TimeDivisionUpsertSheet(
                isEditSheet = false,
                division = TimeDivision(id = 0, name = "", index = divisions.size),
                onDismiss = { showAddSheet = false },
                onUpsert = {
                    onAction(HabitsAction.AddTimeDivision(it))
                    showAddSheet = false
                },
            )
        }
    }
}
