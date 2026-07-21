package com.loc.hexis.shared.ui.note.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loc.hexis.core.LocalDate
import com.loc.hexis.core.LocalDateTime
import com.loc.hexis.core.note.JournalEntry
import com.loc.hexis.core.note.JournalNoteData
import com.loc.hexis.core.note.Note
import com.loc.hexis.core.now
import com.loc.hexis.core.toFormattedString
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.add
import hexis.shared.ui.generated.resources.close
import hexis.shared.ui.generated.resources.delete
import hexis.shared.ui.generated.resources.search
import org.jetbrains.compose.resources.vectorResource
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JournalEditor(
    note: Note,
    onSave: (Note) -> Unit,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    hasEditorCustomColor: Boolean
) {
    val journalData = remember(note.payloadJson) { note.parseJournal() }
    var searchQuery by remember { mutableStateOf("") }
    var newEntryText by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var editingEntryId by remember { mutableStateOf<String?>(null) }
    var editingEntryText by remember { mutableStateOf("") }
    var showMoodMenu by remember { mutableStateOf(false) }

    val today = remember { LocalDate.now() }

    val moods = listOf("😊", "😐", "😢", "😴", "🧠", "🏃")

    val filteredEntries = remember(journalData.entries, searchQuery) {
        val sorted = journalData.entries.sortedBy { it.timestamp } // chronological order for display
        if (searchQuery.isBlank()) sorted
        else sorted.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }

    val groupedEntries = remember(filteredEntries) {
        filteredEntries.groupBy { it.timestamp.date }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search logs…", color = onSurfaceVariantColor) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = onSurfaceColor,
                unfocusedTextColor = onSurfaceColor,
                focusedBorderColor = if (hasEditorCustomColor) onSurfaceColor else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (hasEditorCustomColor) onSurfaceVariantColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant,
            ),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.close),
                            contentDescription = "Clear",
                            tint = onSurfaceVariantColor
                        )
                    }
                } else {
                    Icon(
                        imageVector = vectorResource(Res.drawable.search),
                        contentDescription = null,
                        tint = onSurfaceVariantColor
                    )
                }
            }
        )

        // Journal Entries List
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (journalData.entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No logs yet. Write your first log below!",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = flexFontRounded()),
                            color = onSurfaceVariantColor
                        )
                    }
                }
            } else if (filteredEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching logs found.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = flexFontRounded()),
                            color = onSurfaceVariantColor
                        )
                    }
                }
            } else {
                groupedEntries.forEach { (date, entriesForDate) ->
                    stickyHeader {
                        val headerText = when (val days = date.toEpochDays() - today.toEpochDays()) {
                            0 -> "Today"
                            -1 -> "Yesterday"
                            1 -> "Tomorrow"
                            else -> date.toFormattedString()
                        }
                        Surface(
                            color = if (hasEditorCustomColor) Color.Transparent else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = headerText,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = flexFontEmphasis(),
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                ),
                                color = if (hasEditorCustomColor) onSurfaceColor else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    items(entriesForDate, key = { it.id }) { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Timeline track element on the left
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(24.dp).align(Alignment.Top)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = if (hasEditorCustomColor) onSurfaceColor else MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                        .padding(top = 4.dp)
                                )
                                Spacer(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(48.dp)
                                        .background(color = onSurfaceVariantColor.copy(alpha = 0.2f))
                                )
                            }

                            // Content card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (hasEditorCustomColor) onSurfaceColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = entry.timestamp.time.toFormattedString(is24Hr = false),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = onSurfaceVariantColor
                                            )
                                            if (!entry.mood.isNullOrBlank()) {
                                                Text(
                                                    text = entry.mood,
                                                    modifier = Modifier.padding(start = 8.dp),
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    if (editingEntryId == entry.id) {
                                                        // Save
                                                        val updatedEntries = journalData.entries.map {
                                                            if (it.id == entry.id) it.copy(text = editingEntryText) else it
                                                        }
                                                        onSave(note.withJournal(JournalNoteData(updatedEntries)))
                                                        editingEntryId = null
                                                    } else {
                                                        // Edit Mode
                                                        editingEntryId = entry.id
                                                        editingEntryText = entry.text
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Text(
                                                    text = if (editingEntryId == entry.id) "✓" else "✏️",
                                                    fontSize = 12.sp,
                                                    color = onSurfaceColor
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = {
                                                    val updatedEntries = journalData.entries.filter { it.id != entry.id }
                                                    onSave(note.withJournal(JournalNoteData(updatedEntries)))
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = vectorResource(Res.drawable.delete),
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (editingEntryId == entry.id) {
                                        OutlinedTextField(
                                            value = editingEntryText,
                                            onValueChange = { editingEntryText = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = MaterialTheme.typography.bodyMedium,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = onSurfaceColor,
                                                unfocusedTextColor = onSurfaceColor,
                                                focusedBorderColor = onSurfaceColor,
                                                unfocusedBorderColor = onSurfaceVariantColor.copy(alpha = 0.5f),
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = entry.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = onSurfaceColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Entry Input Row
        Surface(
            color = if (hasEditorCustomColor) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Mood popup picker
                AnimatedVisibility(visible = showMoodMenu) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Mood: ",
                            style = MaterialTheme.typography.labelMedium,
                            color = onSurfaceVariantColor
                        )
                        moods.forEach { mood ->
                            Text(
                                text = mood,
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .clickable {
                                        selectedMood = if (selectedMood == mood) null else mood
                                        showMoodMenu = false
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mood selector button
                    IconButton(onClick = { showMoodMenu = !showMoodMenu }) {
                        Text(
                            text = selectedMood ?: "😊",
                            fontSize = 20.sp
                        )
                    }

                    // Input Text Field
                    OutlinedTextField(
                        value = newEntryText,
                        onValueChange = { newEntryText = it },
                        placeholder = { Text("Log your thoughts…", color = onSurfaceVariantColor) },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (newEntryText.isNotBlank()) {
                                    val newEntry = JournalEntry(
                                        id = "entry_${Random.nextLong(100_000, 999_999)}_${journalData.entries.size}",
                                        timestamp = LocalDateTime.now(),
                                        text = newEntryText.trim(),
                                        mood = selectedMood
                                    )
                                    val updatedEntries = journalData.entries + newEntry
                                    onSave(note.withJournal(JournalNoteData(updatedEntries)))
                                    newEntryText = ""
                                    selectedMood = null
                                    showMoodMenu = false
                                }
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = onSurfaceColor,
                            unfocusedTextColor = onSurfaceColor,
                            focusedBorderColor = if (hasEditorCustomColor) onSurfaceColor else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (hasEditorCustomColor) onSurfaceVariantColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant,
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Add button
                    IconButton(
                        onClick = {
                            if (newEntryText.isNotBlank()) {
                                val newEntry = JournalEntry(
                                    id = "entry_${Random.nextLong(100_000, 999_999)}_${journalData.entries.size}",
                                    timestamp = LocalDateTime.now(),
                                    text = newEntryText.trim(),
                                    mood = selectedMood
                                )
                                val updatedEntries = journalData.entries + newEntry
                                onSave(note.withJournal(JournalNoteData(updatedEntries)))
                                newEntryText = ""
                                selectedMood = null
                                showMoodMenu = false
                            }
                        },
                        colors = if (hasEditorCustomColor) {
                            IconButtonDefaults.filledIconButtonColors(
                                containerColor = onSurfaceColor.copy(alpha = 0.15f),
                                contentColor = onSurfaceColor
                            )
                        } else {
                            IconButtonDefaults.filledIconButtonColors()
                        }
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.add),
                            contentDescription = "Add log",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
