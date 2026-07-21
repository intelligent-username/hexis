package com.loc.hexis.shared.ui.note.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.vectorResource
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JournalEditor(
    note: Note,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onSave: (Note) -> Unit,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    hasEditorCustomColor: Boolean,
) {
    val journalData = remember(note.payloadJson) { note.parseJournal() }
    val entries = remember(journalData) {
        mutableStateListOf<JournalEntry>().apply { addAll(journalData.entries) }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var newEntryText by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var editingEntryId by remember { mutableStateOf<String?>(null) }
    var editingEntryText by remember { mutableStateOf("") }
    var showDescriptionField by remember { mutableStateOf(description.isNotBlank()) }

    val today = remember { LocalDate.now() }
    val moods = listOf("😊", "😐", "😢", "😴", "🧠", "😡", "🥳", "🏃")

    // Sort in REVERSE chronological order (latest entries first)
    val filteredEntries = remember(entries.toList(), searchQuery) {
        val sorted = entries.sortedByDescending { it.timestamp }
        if (searchQuery.isBlank()) sorted
        else sorted.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }

    val groupedEntries = remember(filteredEntries) {
        filteredEntries.groupBy { it.timestamp.date }
    }

    fun save() {
        onSave(note.copy(content = description).withJournal(JournalNoteData(entries.toList())))
    }

    fun addLog() {
        val trimmedText = newEntryText.trim()
        if (trimmedText.isBlank()) return
        val newEntry = JournalEntry(
            id = "entry_${Random.nextLong(100_000_000L, 999_999_999L)}_${entries.size}",
            timestamp = LocalDateTime.now(),
            text = trimmedText,
            mood = selectedMood,
        )
        entries.add(newEntry)
        save()
        newEntryText = ""
        selectedMood = null
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = onSurfaceColor,
        unfocusedTextColor = onSurfaceColor,
        focusedBorderColor = if (hasEditorCustomColor) onSurfaceColor else MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = if (hasEditorCustomColor) onSurfaceVariantColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant,
        focusedPlaceholderColor = onSurfaceVariantColor,
        unfocusedPlaceholderColor = onSurfaceVariantColor,
    )

    val primaryAccent = if (hasEditorCustomColor) onSurfaceColor else MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxSize()) {
        // Centered Header Bar with Title, Description toggle, and Search Overlay
        if (!isSearching) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Left balancing action — toggle description input
                    IconButton(
                        onClick = { showDescriptionField = !showDescriptionField },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Text(
                            text = if (showDescriptionField) "📝" else "📄",
                            fontSize = 18.sp
                        )
                    }

                    // Centered Journal Title Input Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        placeholder = {
                            Text(
                                text = "Journal Title",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = flexFontEmphasis(),
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = flexFontEmphasis(),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = textFieldColors,
                    )

                    // Search Icon Button on the right of title
                    IconButton(
                        onClick = { isSearching = true },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.search),
                            contentDescription = "Search logs",
                            tint = primaryAccent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                // Optional Journal Note Description / Subtitle
                AnimatedVisibility(visible = showDescriptionField) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            onDescriptionChange(it)
                            save()
                        },
                        placeholder = { Text("Journal description or summary notes…") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = flexFontRounded()),
                        colors = textFieldColors,
                        maxLines = 3,
                    )
                }
            }
        } else {
            // Expanded Search Bar Overlay
            AnimatedVisibility(
                visible = isSearching,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search journal logs…", color = onSurfaceVariantColor) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = textFieldColors,
                        leadingIcon = {
                            Icon(
                                imageVector = vectorResource(Res.drawable.search),
                                contentDescription = null,
                                tint = onSurfaceVariantColor,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = vectorResource(Res.drawable.close),
                                        contentDescription = "Clear",
                                        tint = onSurfaceVariantColor,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            isSearching = false
                            searchQuery = ""
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.close),
                            contentDescription = "Close search",
                            tint = onSurfaceVariantColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        // Journal Entries Timeline List (Reverse Chronological)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📖 No journal entries yet",
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = flexFontEmphasis()),
                                color = onSurfaceColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Log your first thought below!",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = flexFontRounded()),
                                color = onSurfaceVariantColor
                            )
                        }
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
                        val headerText = when (date.toEpochDays() - today.toEpochDays()) {
                            0L -> "Today"
                            -1L -> "Yesterday"
                            1L -> "Tomorrow"
                            else -> date.toFormattedString()
                        }
                        Surface(
                            color = if (hasEditorCustomColor) Color.Transparent else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = headerText,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = flexFontEmphasis(),
                                    fontWeight = FontWeight.Bold
                                ),
                                color = primaryAccent
                            )
                        }
                    }

                    itemsIndexed(entriesForDate, key = { _, entry -> entry.id }) { index, entry ->
                        val isFirst = index == 0
                        val isLast = index == entriesForDate.size - 1

                        // Continuous Intrinsic Container Row for seamless connected vertical line graph
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Continuous Connected Timeline Axis Column
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                // Seamless vertical line spanning 100% height of this item
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .fillMaxHeight()
                                        .background(
                                            color = if (hasEditorCustomColor) onSurfaceColor.copy(alpha = 0.25f)
                                            else primaryAccent.copy(alpha = 0.3f)
                                        )
                                )

                                // Timeline Node Circle
                                Surface(
                                    shape = CircleShape,
                                    color = if (hasEditorCustomColor) onSurfaceColor else primaryAccent,
                                    border = BorderStroke(
                                        width = 3.dp,
                                        color = if (hasEditorCustomColor) Color.Transparent
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier
                                        .padding(top = 14.dp)
                                        .size(14.dp)
                                ) {}
                            }

                            // Entry Content Card
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (hasEditorCustomColor) onSurfaceColor.copy(alpha = 0.08f)
                                else MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 6.dp, bottom = 10.dp)
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
                                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = flexFontRounded()),
                                                color = onSurfaceVariantColor
                                            )
                                            val mood = entry.mood
                                            if (!mood.isNullOrBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = onSurfaceVariantColor.copy(alpha = 0.12f),
                                                    modifier = Modifier.padding(start = 8.dp)
                                                ) {
                                                    Text(
                                                        text = mood,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    if (editingEntryId == entry.id) {
                                                        val idx = entries.indexOfFirst { it.id == entry.id }
                                                        if (idx != -1) {
                                                            entries[idx] = entries[idx].copy(text = editingEntryText)
                                                            save()
                                                        }
                                                        editingEntryId = null
                                                    } else {
                                                        editingEntryId = entry.id
                                                        editingEntryText = entry.text
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text(
                                                    text = if (editingEntryId == entry.id) "✓" else "✏️",
                                                    fontSize = 13.sp,
                                                    color = onSurfaceColor
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    entries.removeAll { it.id == entry.id }
                                                    save()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = vectorResource(Res.drawable.delete),
                                                    contentDescription = "Delete entry",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (editingEntryId == entry.id) {
                                        OutlinedTextField(
                                            value = editingEntryText,
                                            onValueChange = { editingEntryText = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = MaterialTheme.typography.bodyMedium,
                                            colors = textFieldColors,
                                        )
                                    } else {
                                        Text(
                                            text = entry.text,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = flexFontRounded()),
                                            color = onSurfaceColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(70.dp)) }
        }

        // Ultra-Modern Floating Bottom Entry Input Panel
        Surface(
            color = if (hasEditorCustomColor) onSurfaceColor.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Interactive Emoji Mood Pills (Horizontal Scroll Bar)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mood:",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded()),
                        color = onSurfaceVariantColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    moods.forEach { mood ->
                        val isSelected = selectedMood == mood
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) primaryAccent else onSurfaceVariantColor.copy(alpha = 0.12f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    selectedMood = if (isSelected) null else mood
                                }
                        ) {
                            Text(
                                text = mood,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Thought Input & Send Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newEntryText,
                        onValueChange = { newEntryText = it },
                        placeholder = { Text("Log your thought or reflection…", color = onSurfaceVariantColor) },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { addLog() },
                            onDone = { addLog() }
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = textFieldColors,
                        maxLines = 4,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send / Add Log Action Button
                    FilledTonalIconButton(
                        onClick = ::addLog,
                        enabled = newEntryText.isNotBlank(),
                        colors = if (hasEditorCustomColor) {
                            IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = onSurfaceColor.copy(alpha = 0.2f),
                                contentColor = onSurfaceColor
                            )
                        } else {
                            IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.add),
                            contentDescription = "Log thought",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
