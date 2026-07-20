package com.loc.hexis.shared.ui.note.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.note.CounterRow
import com.loc.hexis.core.note.CountingTableData
import com.loc.hexis.core.note.Note
import com.loc.hexis.core.note.NoteRepo
import com.loc.hexis.core.note.NoteType
import com.loc.hexis.core.now
import com.loc.hexis.shared.ui.app.SystemBackHandler
import com.loc.hexis.shared.ui.components.Empty
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.add
import hexis.shared.ui.generated.resources.archive
import hexis.shared.ui.generated.resources.archived_notes
import hexis.shared.ui.generated.resources.check
import hexis.shared.ui.generated.resources.close
import hexis.shared.ui.generated.resources.delete
import hexis.shared.ui.generated.resources.drag_indicator
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
import kotlin.random.Random
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
    val haptic = LocalHapticFeedback.current
    val gridState = rememberLazyStaggeredGridState()
    val listState = rememberLazyListState()
    val notes = remember { mutableStateListOf<Note>() }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showCreateTypeSheet by remember { mutableStateOf(false) }
    var isGridMode by remember { mutableStateOf(true) }
    var notesLoaded by remember { mutableStateOf(false) }
    var showArchived by remember { mutableStateOf(false) }

    // Multi-select state
    var selectedNoteIds by remember { mutableStateOf(emptySet<Long>()) }
    val isSelectionMode = selectedNoteIds.isNotEmpty()

    // Drag to reorder state
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // Undo state
    var undoMessage by remember { mutableStateOf("") }
    var undoAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var undoVisible by remember { mutableStateOf(false) }
    var undoKey by remember { mutableStateOf(0) }

    val msgNoteArchived = stringResource(Res.string.note_archived)
    val msgNoteUnarchived = stringResource(Res.string.note_unarchived)
    val msgNoteDeleted = stringResource(Res.string.note_deleted)

    LaunchedEffect(showArchived) {
        val flow = if (showArchived) repo.getArchivedNotesFlow() else repo.getNotesFlow()
        flow.collect { list ->
            notes.clear()
            notes.addAll(list)
            notesLoaded = true
        }
    }

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
        remember(notes.toList(), searchQuery, showArchived) {
            val list =
                if (searchQuery.isBlank()) notes
                else
                    notes.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                            it.content.contains(searchQuery, ignoreCase = true)
                    }
            if (showArchived) list
            else list.sortedWith(compareByDescending<Note> { it.pinned }.thenBy { it.sortOrder })
        }

    fun toggleSelectNote(id: Long) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        selectedNoteIds =
            if (selectedNoteIds.contains(id)) selectedNoteIds - id else selectedNoteIds + id
    }

    SystemBackHandler(enabled = true) {
        if (isSelectionMode) {
            selectedNoteIds = emptySet()
        } else if (showCreateTypeSheet) {
            showCreateTypeSheet = false
        } else if (showEditor) {
            showEditor = false
            editingNote = null
        } else {
            onDismiss()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (isSelectionMode) {
                    // Multi-select header bar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { selectedNoteIds = emptySet() },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.close),
                                contentDescription = "Cancel Selection",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        Text(
                            text = "${selectedNoteIds.size} Selected",
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = flexFontEmphasis()
                                ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        // Select All / Deselect All Button
                        TextButton(
                            onClick = {
                                selectedNoteIds =
                                    if (selectedNoteIds.size == filteredNotes.size) emptySet()
                                    else filteredNotes.map { it.id }.toSet()
                            }
                        ) {
                            Text(
                                text = if (selectedNoteIds.size == filteredNotes.size) "Deselect" else "All",
                                fontFamily = flexFontRounded(),
                            )
                        }

                        // Batch Delete Button
                        IconButton(
                            onClick = {
                                val toDelete = notes.filter { selectedNoteIds.contains(it.id) }
                                scope.launch {
                                    toDelete.forEach { repo.deleteNote(it.id) }
                                    selectedNoteIds = emptySet()
                                    showUndo("${toDelete.size} notes deleted") {
                                        scope.launch {
                                            toDelete.forEach { repo.upsertNote(it) }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.delete),
                                contentDescription = "Delete Selected",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                } else {
                    // Standard header bar
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
                        // Grid / List Mode Toggle Button
                        FilledTonalIconButton(
                            onClick = { isGridMode = !isGridMode },
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
                            Text(
                                text = if (isGridMode) "☰" else "⊞",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }

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
            }

            // Search bar
            AnimatedVisibility(
                visible = showSearch && !isSelectionMode,
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

            // Notes Content Grid / List
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
            } else if (isGridMode) {
                LazyVerticalStaggeredGrid(
                    state = gridState,
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp,
                ) {
                    itemsIndexed(filteredNotes, key = { _, note -> note.id }) { index, note ->
                        val isDragging = draggingIndex == index
                        val isSelected = selectedNoteIds.contains(note.id)

                        Box(
                            modifier =
                                Modifier.animateItem()
                                    .graphicsLayer {
                                        scaleX = if (isDragging) 1.05f else 1.0f
                                        scaleY = if (isDragging) 1.05f else 1.0f
                                        shadowElevation = if (isDragging) 16.dp.toPx() else 0f
                                        translationX = if (isDragging) dragOffset.x else 0f
                                        translationY = if (isDragging) dragOffset.y else 0f
                                        alpha = if (isDragging) 0.88f else 1.0f
                                    }
                        ) {
                            Row {
                                // Dedicated drag grip — immediate drag, no long-press delay
                                if (!showArchived && !isSelectionMode && searchQuery.isEmpty()) {
                                    DragGrip(
                                        onDragStart = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            draggingIndex = index
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            dragOffset += amount
                                            draggingIndex?.let { currentIdx ->
                                                val targetIdx =
                                                    findHitItemIndex(
                                                        gridState = gridState,
                                                        draggedIndex = currentIdx,
                                                        currentOffset = dragOffset,
                                                    )
                                                if (targetIdx != null && targetIdx != currentIdx) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    val item = notes.removeAt(currentIdx)
                                                    notes.add(targetIdx, item)
                                                    draggingIndex = targetIdx
                                                    dragOffset = Offset.Zero
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            draggingIndex?.let {
                                                val orderMap = notes.mapIndexed { i, n -> n.id to i }.toMap()
                                                scope.launch { repo.updateSortOrders(orderMap) }
                                            }
                                            draggingIndex = null
                                            dragOffset = Offset.Zero
                                        },
                                        onDragCancel = {
                                            draggingIndex = null
                                            dragOffset = Offset.Zero
                                        },
                                    )
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    if (note.type == NoteType.COUNTING_TABLE) {
                                        CountingTableCard(
                                            note = note,
                                            showArchived = showArchived,
                                            onClick = {
                                                if (isSelectionMode) toggleSelectNote(note.id)
                                                else {
                                                    editingNote = note
                                                    showEditor = true
                                                }
                                            },
                                            onLongClick = {
                                                if (!isSelectionMode) toggleSelectNote(note.id)
                                            },
                                            onValueChange = { rowId, newValue ->
                                                val data = note.parseCountingTable()
                                                val updatedRows =
                                                    data.rows.map { r ->
                                                        if (r.id == rowId) r.copy(value = newValue) else r
                                                    }
                                                val updatedNote = note.withCountingTable(CountingTableData(updatedRows))
                                                scope.launch { repo.upsertNote(updatedNote) }
                                            },
                                            onTogglePin = {
                                                scope.launch { repo.upsertNote(note.copy(pinned = !note.pinned)) }
                                            },
                                            onArchive = {
                                                scope.launch {
                                                    repo.upsertNote(note.copy(archived = true))
                                                    showUndo(msgNoteArchived) {
                                                        scope.launch { repo.upsertNote(note.copy(archived = false)) }
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
                                    } else {
                                        NoteCard(
                                            note = note,
                                            showArchived = showArchived,
                                            onClick = {
                                                if (isSelectionMode) toggleSelectNote(note.id)
                                                else {
                                                    editingNote = note
                                                    showEditor = true
                                                }
                                            },
                                            onLongClick = {
                                                if (!isSelectionMode) toggleSelectNote(note.id)
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

                                    // Selection Checkbox Overlay
                                    if (isSelectionMode) {
                                        Box(
                                            modifier =
                                                Modifier.align(Alignment.TopEnd)
                                                    .padding(8.dp)
                                                    .size(24.dp)
                                                    .background(
                                                        color =
                                                            if (isSelected) MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
                                                        shape = CircleShape,
                                                    )
                                                    .border(
                                                        width = 2.dp,
                                                        color =
                                                            if (isSelected) MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.outline,
                                                        shape = CircleShape,
                                                    ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = vectorResource(Res.drawable.check),
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(14.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(filteredNotes, key = { _, note -> note.id }) { index, note ->
                        val isDragging = draggingIndex == index
                        val isSelected = selectedNoteIds.contains(note.id)

                        Box(
                            modifier =
                                Modifier.animateItem()
                                    .graphicsLayer {
                                        scaleX = if (isDragging) 1.03f else 1.0f
                                        scaleY = if (isDragging) 1.03f else 1.0f
                                        shadowElevation = if (isDragging) 16.dp.toPx() else 0f
                                        translationY = if (isDragging) dragOffset.y else 0f
                                        alpha = if (isDragging) 0.88f else 1.0f
                                    }
                        ) {
                            Row {
                                // Dedicated drag grip — immediate drag, no long-press delay
                                if (!showArchived && !isSelectionMode && searchQuery.isEmpty()) {
                                    DragGrip(
                                        onDragStart = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            draggingIndex = index
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            dragOffset += amount
                                            draggingIndex?.let { currentIdx ->
                                                val targetIdx =
                                                    findHitItemIndexList(
                                                        listState = listState,
                                                        draggedIndex = currentIdx,
                                                        currentOffset = dragOffset,
                                                    )
                                                if (targetIdx != null && targetIdx != currentIdx) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    val item = notes.removeAt(currentIdx)
                                                    notes.add(targetIdx, item)
                                                    draggingIndex = targetIdx
                                                    dragOffset = Offset.Zero
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            draggingIndex?.let {
                                                val orderMap = notes.mapIndexed { i, n -> n.id to i }.toMap()
                                                scope.launch { repo.updateSortOrders(orderMap) }
                                            }
                                            draggingIndex = null
                                            dragOffset = Offset.Zero
                                        },
                                        onDragCancel = {
                                            draggingIndex = null
                                            dragOffset = Offset.Zero
                                        },
                                    )
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    if (note.type == NoteType.COUNTING_TABLE) {
                                        CountingTableCard(
                                            note = note,
                                            showArchived = showArchived,
                                            onClick = {
                                                if (isSelectionMode) toggleSelectNote(note.id)
                                                else {
                                                    editingNote = note
                                                    showEditor = true
                                                }
                                            },
                                            onLongClick = {
                                                if (!isSelectionMode) toggleSelectNote(note.id)
                                            },
                                            onValueChange = { rowId, newValue ->
                                                val data = note.parseCountingTable()
                                                val updatedRows =
                                                    data.rows.map { r ->
                                                        if (r.id == rowId) r.copy(value = newValue) else r
                                                    }
                                                val updatedNote = note.withCountingTable(CountingTableData(updatedRows))
                                                scope.launch { repo.upsertNote(updatedNote) }
                                            },
                                            onTogglePin = {
                                                scope.launch { repo.upsertNote(note.copy(pinned = !note.pinned)) }
                                            },
                                            onArchive = {
                                                scope.launch {
                                                    repo.upsertNote(note.copy(archived = true))
                                                    showUndo(msgNoteArchived) {
                                                        scope.launch { repo.upsertNote(note.copy(archived = false)) }
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
                                    } else {
                                        NoteCard(
                                            note = note,
                                            showArchived = showArchived,
                                            onClick = {
                                                if (isSelectionMode) toggleSelectNote(note.id)
                                                else {
                                                    editingNote = note
                                                    showEditor = true
                                                }
                                            },
                                            onLongClick = {
                                                if (!isSelectionMode) toggleSelectNote(note.id)
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

                                    // Selection Checkbox Overlay
                                    if (isSelectionMode) {
                                        Box(
                                            modifier =
                                                Modifier.align(Alignment.TopEnd)
                                                    .padding(10.dp)
                                                    .size(24.dp)
                                                    .background(
                                                        color =
                                                            if (isSelected) MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
                                                        shape = CircleShape,
                                                    )
                                                    .border(
                                                        width = 2.dp,
                                                        color =
                                                            if (isSelected) MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.outline,
                                                        shape = CircleShape,
                                                    ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = vectorResource(Res.drawable.check),
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(14.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB — only in active view and not selecting
        if (!showArchived && !isSelectionMode) {
            MediumFloatingActionButton(
                onClick = { showCreateTypeSheet = true },
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

    // Bottom sheet for choosing note creation type
    if (showCreateTypeSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showCreateTypeSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Create New Note",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = flexFontEmphasis()),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier =
                        Modifier.fillMaxWidth().clickable {
                            showCreateTypeSheet = false
                            val now = LocalDateTime.now()
                            val newId = Random.nextLong(100_000_000L, 999_999_999L)
                            editingNote =
                                Note(
                                    id = newId,
                                    title = "",
                                    content = "",
                                    type = NoteType.MARKDOWN,
                                    createdAt = now,
                                    updatedAt = now,
                                )
                            showEditor = true
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text("📝", style = MaterialTheme.typography.headlineMedium)
                        Column {
                            Text(
                                text = "Markdown Note",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                            )
                            Text(
                                text = "Free-form text, headers, lists, and formatting",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier =
                        Modifier.fillMaxWidth().clickable {
                            showCreateTypeSheet = false
                            val now = LocalDateTime.now()
                            val newId = Random.nextLong(100_000_000L, 999_999_999L)
                            val initialRow = CounterRow(id = "row_${Random.nextLong(100_000, 999_999)}_0", label = "")
                            editingNote =
                                Note(
                                    id = newId,
                                    title = "",
                                    content = "",
                                    type = NoteType.COUNTING_TABLE,
                                    createdAt = now,
                                    updatedAt = now,
                                ).withCountingTable(CountingTableData(rows = listOf(initialRow)))
                            showEditor = true
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text("📊", style = MaterialTheme.typography.headlineMedium)
                        Column {
                            Text(
                                text = "Counting Table Note",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                            )
                            Text(
                                text = "Interactive tally counters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
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
            },
            onArchive = { noteId ->
                scope.launch {
                    val noteToArchive = notes.firstOrNull { it.id == noteId } ?: editingNote
                    if (noteToArchive != null) {
                        val newArchivedState = !noteToArchive.archived
                        repo.upsertNote(noteToArchive.copy(archived = newArchivedState))
                        showUndo(if (newArchivedState) msgNoteArchived else msgNoteUnarchived) {
                            scope.launch { repo.upsertNote(noteToArchive) }
                        }
                    }
                }
            },
        )
    }
}

/**
 * A narrow drag grip that uses [detectDragGestures] (immediate, no long-press delay).
 * Sits on the left edge of each note card and communicates drag events back to the caller.
 */
@Composable
private fun DragGrip(
    onDragStart: () -> Unit,
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(28.dp)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onDragStart() },
                        onDrag = onDrag,
                        onDragEnd = onDragEnd,
                        onDragCancel = onDragCancel,
                    )
                },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = vectorResource(Res.drawable.drag_indicator),
            contentDescription = "Drag to reorder",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp),
        )
    }
}

private fun findHitItemIndex(
    gridState: LazyStaggeredGridState,
    draggedIndex: Int,
    currentOffset: Offset,
): Int? {
    val visibleItems = gridState.layoutInfo.visibleItemsInfo
    val draggedItemInfo = visibleItems.firstOrNull { it.index == draggedIndex } ?: return null

    val draggedCenterX = draggedItemInfo.offset.x + (draggedItemInfo.size.width / 2) + currentOffset.x
    val draggedCenterY = draggedItemInfo.offset.y + (draggedItemInfo.size.height / 2) + currentOffset.y

    return visibleItems.firstOrNull { item ->
        item.index != draggedIndex &&
            draggedCenterX >= item.offset.x &&
            draggedCenterX <= item.offset.x + item.size.width &&
            draggedCenterY >= item.offset.y &&
            draggedCenterY <= item.offset.y + item.size.height
    }?.index
}

private fun findHitItemIndexList(
    listState: LazyListState,
    draggedIndex: Int,
    currentOffset: Offset,
): Int? {
    val visibleItems = listState.layoutInfo.visibleItemsInfo
    val draggedItemInfo = visibleItems.firstOrNull { it.index == draggedIndex } ?: return null

    val draggedCenterY = draggedItemInfo.offset + (draggedItemInfo.size / 2) + currentOffset.y

    return visibleItems.firstOrNull { item ->
        item.index != draggedIndex &&
            draggedCenterY >= item.offset &&
            draggedCenterY <= item.offset + item.size
    }?.index
}

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

