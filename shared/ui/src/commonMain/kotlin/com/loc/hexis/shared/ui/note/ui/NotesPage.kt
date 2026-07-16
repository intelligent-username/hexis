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

package com.loc.hexis.shared.ui.note.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.IconToggleButtonShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.note.Note
import com.loc.hexis.core.note.NoteRepo
import com.loc.hexis.core.now
import com.loc.hexis.shared.ui.app.SystemBackHandler
import com.loc.hexis.shared.ui.components.Empty
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.add
import hexis.shared.ui.generated.resources.archive
import hexis.shared.ui.generated.resources.archived_notes
import hexis.shared.ui.generated.resources.close
import hexis.shared.ui.generated.resources.new_note
import hexis.shared.ui.generated.resources.no_notes_found
import hexis.shared.ui.generated.resources.note_archived
import hexis.shared.ui.generated.resources.note_deleted
import hexis.shared.ui.generated.resources.note_unarchived
import hexis.shared.ui.generated.resources.notes
import hexis.shared.ui.generated.resources.search
import hexis.shared.ui.generated.resources.search_notes
import hexis.shared.ui.generated.resources.unarchive
import hexis.shared.ui.generated.resources.undo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesPage(onDismiss: () -> Unit, repo: NoteRepo = koinInject()) {
    val scope = rememberCoroutineScope()
    val notes = remember { mutableStateListOf<Note>() }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var notesLoaded by remember { mutableStateOf(false) }
    var showArchived by remember { mutableStateOf(false) }

    // Undo state
    var undoMessage by remember { mutableStateOf("") }
    var undoAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var undoVisible by remember { mutableStateOf(false) }
    var undoKey by remember { mutableStateOf(0) }

    val msgNoteArchived = stringResource(Res.string.note_archived)
    val msgNoteUnarchived = stringResource(Res.string.note_unarchived)
    val msgNoteDeleted = stringResource(Res.string.note_deleted)

    // Collect flow based on archive toggle
    LaunchedEffect(showArchived) {
        val flow = if (showArchived) repo.getArchivedNotesFlow() else repo.getNotesFlow()
        flow.collect { list ->
            notes.clear()
            notes.addAll(list)
            notesLoaded = true
        }
    }

    // Auto-dismiss undo snackbar after 5 seconds
    LaunchedEffect(undoKey) {
        if (undoVisible) {
            delay(5000)
            undoVisible = false
            undoAction = null
        }
    }

    fun showUndo(message: String, onUndo: () -> Unit) {
        undoMessage = message
        undoAction = onUndo
        undoVisible = true
        undoKey++
    }

    val filteredNotes =
        if (searchQuery.isBlank()) notes
        else
            remember(notes, searchQuery) {
                notes.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true)
                }
            }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    SystemBackHandler(enabled = true) {
        if (showEditor) {
            showEditor = false
            editingNote = null
        } else {
            onDismiss()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.close),
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    Text(
                        text =
                            if (showArchived) stringResource(Res.string.archived_notes)
                            else stringResource(Res.string.notes),
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = flexFontEmphasis()
                            ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (notes.isNotEmpty() && !showArchived) {
                        FilledTonalIconButton(
                            onClick = {
                                showSearch = !showSearch
                                if (!showSearch) searchQuery = ""
                            },
                            modifier = Modifier.size(40.dp),
                            colors =
                                IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            shapes =
                                IconButtonShapes(
                                    shape = RoundedCornerShape(12.dp),
                                    pressedShape = MaterialTheme.shapes.small,
                                ),
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.search),
                                contentDescription = "Search",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    FilledTonalIconToggleButton(
                        checked = showArchived,
                        modifier = Modifier.size(40.dp),
                        shapes =
                            IconToggleButtonShapes(
                                shape = CircleShape,
                                checkedShape = MaterialTheme.shapes.small,
                                pressedShape = MaterialTheme.shapes.extraSmall,
                            ),
                        onCheckedChange = { showArchived = it },
                    ) {
                        Icon(
                            painter =
                                painterResource(
                                    if (showArchived) Res.drawable.unarchive
                                    else Res.drawable.archive
                                ),
                            contentDescription =
                                if (showArchived) "Show active" else "Show archived",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // Search bar
            AnimatedVisibility(
                visible = showSearch,
                enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
                exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(Res.string.search_notes)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.large,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.close),
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    },
                )
            }

            // Notes list
            if (!notesLoaded) {
                Spacer(modifier = Modifier.weight(1f))
            } else if (filteredNotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (searchQuery.isNotEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(Res.string.no_notes_found),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        Empty(modifier = Modifier.padding(top = 150.dp))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            showArchived = showArchived,
                            onClick = {
                                editingNote = note
                                showEditor = true
                            },
                            onTogglePin = {
                                scope.launch { repo.upsertNote(note.copy(pinned = !note.pinned)) }
                            },
                            onArchive = {
                                scope.launch {
                                    repo.upsertNote(note.copy(archived = true))
                                    showUndo(msgNoteArchived) {
                                        scope.launch {
                                            repo.upsertNote(note.copy(archived = false))
                                        }
                                    }
                                }
                            },
                            onUnarchive = {
                                scope.launch {
                                    repo.upsertNote(note.copy(archived = false))
                                    showUndo(msgNoteUnarchived) {
                                        scope.launch { repo.upsertNote(note.copy(archived = true)) }
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    val deleted = note
                                    repo.deleteNote(note.id)
                                    showUndo(msgNoteDeleted) {
                                        scope.launch { repo.upsertNote(deleted) }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }

        // FAB — only in active view
        if (!showArchived) {
            MediumFloatingActionButton(
                onClick = {
                    showEditor = true
                    editingNote = null
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier =
                    Modifier.align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .animateFloatingActionButton(
                            visible = true,
                            alignment = Alignment.BottomEnd,
                            scaleAnimationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                            alphaAnimationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                        ),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.add),
                        contentDescription = stringResource(Res.string.new_note),
                        modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                    )
                    AnimatedVisibility(
                        visible = filteredNotes.isEmpty(),
                        enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
                        exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
                    ) {
                        Text(
                            text = stringResource(Res.string.new_note),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }

        // Undo snackbar at bottom-right
        AnimatedVisibility(
            visible = undoVisible,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()) + slideInVertically(),
            exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) + slideOutVertically(),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 6.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = undoMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                    TextButton(
                        onClick = {
                            undoAction?.invoke()
                            undoVisible = false
                        },
                        shapes =
                            ButtonShapes(
                                shape = MaterialTheme.shapes.small,
                                pressedShape = MaterialTheme.shapes.extraSmall,
                            ),
                    ) {
                        Text(
                            stringResource(Res.string.undo),
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = flexFontRounded(),
                        )
                    }
                }
            }
        }
    }

    // Editor bottom sheet
    if (showEditor) {
        NoteEditorSheet(
            note = editingNote,
            onDismissRequest = {
                showEditor = false
                editingNote = null
            },
            onSave = { saved ->
                scope.launch { repo.upsertNote(saved) }
                showEditor = false
                editingNote = null
            },
        )
    }
}

/**
 * Formats a [LocalDateTime] to a short human-readable date string. Shows "Today" or "Yesterday" for
 * recent dates, or a formatted date like "Jan 15".
 */
internal fun formatNoteDate(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val today = now.date
    val date = dateTime.date

    return when {
        date == today -> "Today"
        date == today.minus(1, kotlinx.datetime.DateTimeUnit.DAY) -> "Yesterday"
        else -> {
            val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
            "$month ${date.dayOfMonth}"
        }
    }
}
