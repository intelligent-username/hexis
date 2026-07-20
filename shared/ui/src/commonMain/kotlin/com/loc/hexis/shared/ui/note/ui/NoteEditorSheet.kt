package com.loc.hexis.shared.ui.note.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loc.hexis.core.note.CounterRow
import com.loc.hexis.core.note.CountingTableData
import com.loc.hexis.core.note.Note
import com.loc.hexis.core.note.NoteType
import com.loc.hexis.core.now
import com.loc.hexis.shared.ui.app.SystemBackHandler
import com.loc.hexis.shared.ui.components.FloatingLabelTextField
import com.loc.hexis.shared.ui.note.getNextListPrefix
import com.loc.hexis.shared.ui.note.removePrefix
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.add
import hexis.shared.ui.generated.resources.archive
import hexis.shared.ui.generated.resources.close
import hexis.shared.ui.generated.resources.edit_note
import hexis.shared.ui.generated.resources.new_note
import hexis.shared.ui.generated.resources.title
import hexis.shared.ui.generated.resources.unarchive
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun NoteEditorSheet(
    note: Note?,
    onDismissRequest: () -> Unit,
    onSave: (Note) -> Unit,
    onArchive: ((Long) -> Unit)? = null,
) {
    val initialNoteId = remember { note?.id ?: 0L }
    var title by remember(initialNoteId) { mutableStateOf(note?.title ?: "") }
    var selectedType by remember(initialNoteId) { mutableStateOf(note?.type ?: NoteType.MARKDOWN) }

    var contentValue by remember(initialNoteId) {
        mutableStateOf(
            TextFieldValue((note?.content ?: "").replace("\r\n", "\n").replace('\r', '\n'))
        )
    }

    val initialTableData = remember(initialNoteId) { note?.parseCountingTable() ?: CountingTableData() }
    val counterRows = remember(initialNoteId) {
        mutableStateListOf<CounterRow>().apply { addAll(initialTableData.rows) }
    }

    var isSaved by remember { mutableStateOf(true) }
    var currentNoteId by remember(initialNoteId) { mutableStateOf(initialNoteId) }
    val contentFocusRequester = remember { FocusRequester() }
    val markdownScrollState = rememberScrollState()

    fun buildCurrentNote(): Note {
        val nowTime = LocalDateTime.now()
        val baseNote =
            (note
                    ?: Note(
                        id = currentNoteId,
                        title = "",
                        content = "",
                        createdAt = nowTime,
                        updatedAt = nowTime,
                    ))
                .copy(id = currentNoteId, title = title, type = selectedType, updatedAt = nowTime)

        return if (selectedType == NoteType.COUNTING_TABLE) {
            baseNote.withCountingTable(CountingTableData(rows = counterRows.toList()))
        } else {
            baseNote.copy(content = contentValue.text)
        }
    }

    SystemBackHandler(enabled = true) {
        onSave(buildCurrentNote())
        onDismissRequest()
    }

    // Auto-save debounce (1500ms)
    LaunchedEffect(title, selectedType, contentValue.text, counterRows.toList()) {
        isSaved = false
        delay(1500)
        val noteToSave = buildCurrentNote()
        onSave(noteToSave)
        isSaved = true
    }

    val wordCount = remember(contentValue.text) {
        if (contentValue.text.isBlank()) 0
        else contentValue.text.trim().split(Regex("""\s+""")).size
    }
    val charCount = remember(contentValue.text) { contentValue.text.length }
    var collapsedHeaderLines by remember { mutableStateOf(setOf<Int>()) }
    var lastToggledHeaderLine by remember { mutableStateOf(-1) }
    LaunchedEffect(contentValue.selection.start) {
        val cursor = contentValue.selection.start
        val text = contentValue.text
        if (text.isNotEmpty() && cursor <= text.length) {
            val lineIndex = text.substring(0, cursor).count { it == '\n' }
            val lines = text.lines()
            val trimmed = lines.getOrNull(lineIndex)?.trimStart() ?: ""
            if (trimmed.startsWith("# ") || trimmed.startsWith("## ") || trimmed.startsWith("### ")) {
                if (lastToggledHeaderLine != lineIndex) {
                    lastToggledHeaderLine = lineIndex
                    collapsedHeaderLines =
                        if (lineIndex in collapsedHeaderLines) collapsedHeaderLines - lineIndex
                        else collapsedHeaderLines + lineIndex
                }
            } else {
                lastToggledHeaderLine = -1
            }
        }
    }

    fun getCurrentLine(text: String, cursor: Int): String {
        val before = text.substring(0, cursor.coerceAtMost(text.length))
        val lineStart = before.lastIndexOf('\n') + 1
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        return text.substring(lineStart, lineEnd)
    }

    fun replaceCurrentLine(text: String, cursor: Int, newLine: String): String {
        val before = text.substring(0, cursor.coerceAtMost(text.length))
        val lineStart = before.lastIndexOf('\n') + 1
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        return text.substring(0, lineStart) + newLine + text.substring(lineEnd)
    }

    fun toggleFormat(prefix: String) {
        val text = contentValue.text
        val cursor = contentValue.selection.start
        val currentLine = getCurrentLine(text, cursor)
        val cleanLine = removePrefix(currentLine)
        val newLine = if (currentLine.startsWith(prefix)) cleanLine else "$prefix$cleanLine"
        val newText = replaceCurrentLine(text, cursor, newLine)
        val lineStart = text.substring(0, cursor.coerceAtMost(text.length)).lastIndexOf('\n') + 1
        val newCursor = (lineStart + newLine.length).coerceAtMost(newText.length)
        contentValue = TextFieldValue(newText, TextRange(newCursor))
    }

    val focusManager = LocalFocusManager.current

    Surface(
        modifier =
            Modifier.fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    focusManager.clearFocus()
                },
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        onSave(buildCurrentNote())
                        onDismissRequest()
                    }
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.close),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    Text(
                        text =
                            if (note == null) stringResource(Res.string.new_note)
                            else stringResource(Res.string.edit_note),
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = flexFontEmphasis()),
                    )
                    if (selectedType == NoteType.MARKDOWN) {
                        Text(
                            text = "$wordCount words • $charCount chars",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = "${counterRows.size} counter items",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (currentNoteId != 0L && onArchive != null) {
                    val isArchived = note?.archived == true
                    IconButton(
                        onClick = {
                            onArchive(currentNoteId)
                            onDismissRequest()
                        },
                        modifier = Modifier.padding(end = 4.dp),
                    ) {
                        Icon(
                            imageVector =
                                vectorResource(
                                    if (isArchived) Res.drawable.unarchive else Res.drawable.archive
                                ),
                            contentDescription = if (isArchived) "Unarchive Note" else "Archive Note",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                Button(
                    onClick = {
                        onSave(buildCurrentNote())
                        onDismissRequest()
                    },
                    shapes = ButtonShapes(shape = CircleShape, pressedShape = CircleShape),
                    colors =
                        if (isSaved)
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        else
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = if (isSaved) "Saved" else "Done",
                        fontFamily = flexFontRounded(),
                    )
                }
            }



            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        stringResource(Res.string.title),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                shape = RoundedCornerShape(16.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedType == NoteType.MARKDOWN) {
                // Main Markdown Field with Scroll Room & Tap-to-Focus Container
                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(markdownScrollState)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    contentFocusRequester.requestFocus()
                                    if (contentValue.text.isEmpty()) {
                                        contentValue = TextFieldValue("", TextRange(0))
                                    }
                                },
                            ),
                ) {
                    OutlinedTextField(
                        value = contentValue,
                        onValueChange = { newValue ->
                            if (newValue.text.contains('\r')) {
                                val cleanText = newValue.text.replace("\r\n", "\n").replace('\r', '\n')
                                val cursorOffset =
                                    newValue.text
                                        .substring(0, newValue.selection.start.coerceAtMost(newValue.text.length))
                                        .count { it == '\r' }
                                val newCursor = (newValue.selection.start - cursorOffset).coerceIn(0, cleanText.length)
                                contentValue = TextFieldValue(cleanText, TextRange(newCursor))
                            } else {
                                contentValue = newValue
                            }
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        visualTransformation =
                            NoteVisualTransformation(
                                primaryColor = MaterialTheme.colorScheme.primary,
                                mutedColor = MaterialTheme.colorScheme.outline,
                                ruleColor = MaterialTheme.colorScheme.primaryContainer,
                                collapsedLineIndices = collapsedHeaderLines,
                            ),
                        placeholder = {
                            Text("Start writing\u2026", style = MaterialTheme.typography.bodyLarge)
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
                        modifier =
                            Modifier.fillMaxWidth()
                                .focusRequester(contentFocusRequester)
                                .onPreviewKeyEvent { event ->
                                    if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                                        val text = contentValue.text
                                        val cursor = contentValue.selection.start
                                        val currentLine = getCurrentLine(text, cursor)
                                        val lineStart = text.substring(0, cursor.coerceAtMost(text.length)).lastIndexOf('\n') + 1

                                        // Only attempt list auto-continue if cursor is NOT at start of line
                                        if (cursor > lineStart) {
                                            val prefix = getNextListPrefix(currentLine)
                                            if (prefix != null) {
                                                val before = text.substring(0, cursor.coerceAtMost(text.length))
                                                val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it + 1 }
                                                val after = text.substring(lineEnd)
                                                val cleanLine = removePrefix(currentLine).trim()
                                                if (cleanLine.isEmpty()) {
                                                    val newText = text.substring(0, lineStart) + after
                                                    contentValue = TextFieldValue(newText, TextRange(lineStart))
                                                } else {
                                                    val newText = before + "\n" + prefix + after
                                                    val newCursor = before.length + 1 + prefix.length
                                                    contentValue = TextFieldValue(newText, TextRange(newCursor))
                                                }
                                                true
                                            } else false
                                        } else false
                                    } else false
                                },
                        shape = RoundedCornerShape(20.dp),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            ),
                    )

                    // Generous bottom scrolling space so text is easily visible above the keyboard
                    Spacer(modifier = Modifier.height(180.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toolbar Container
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val currentText = contentValue.text
                        val currentCursor = contentValue.selection.start
                        val currentLine = getCurrentLine(currentText, currentCursor)

                        listOf(
                            "H1" to "# ",
                            "H2" to "## ",
                            "H3" to "### ",
                        ).forEach { (label, prefix) ->
                            val isActive = currentLine.startsWith(prefix)
                            FilledTonalButton(
                                onClick = { toggleFormat(prefix) },
                                modifier = Modifier.height(34.dp),
                                shape = CircleShape,
                                colors =
                                    if (isActive)
                                        ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    else
                                        ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            contentColor = MaterialTheme.colorScheme.onSurface,
                                        ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = flexFontRounded()),
                                )
                            }
                        }

                        val allHeaderIndices = remember(currentText) {
                            currentText.lines().mapIndexedNotNull { idx, line ->
                                val trimmed = line.trimStart()
                                if (trimmed.startsWith("# ") || trimmed.startsWith("## ") || trimmed.startsWith("### ")) idx else null
                            }
                        }
                        if (allHeaderIndices.isNotEmpty()) {
                            val isAnyCollapsed = collapsedHeaderLines.isNotEmpty()
                            FilledTonalButton(
                                onClick = {
                                    collapsedHeaderLines = if (isAnyCollapsed) emptySet() else allHeaderIndices.toSet()
                                },
                                modifier = Modifier.height(34.dp),
                                shape = CircleShape,
                                colors =
                                    ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            ) {
                                Text(
                                    text = if (isAnyCollapsed) "Expand All" else "Collapse All",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = flexFontRounded()),
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                val text = contentValue.text
                                val cursor = contentValue.selection.start
                                val newText = text.substring(0, cursor) + "---\n" + text.substring(cursor)
                                contentValue = TextFieldValue(newText, TextRange(cursor + 4))
                            },
                            modifier = Modifier.height(34.dp),
                            shape = CircleShape,
                            colors =
                                ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        ) {
                            Text(
                                text = "\u2501\u2501",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = flexFontRounded()),
                            )
                        }
                    }
                }
            } else {
                // Counting Table Editor Rows with Extra Bottom Scroll Room
                Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Counters",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                        )
                        FilledTonalButton(
                            onClick = {
                                val newId = "row_${Random.nextLong(100_000, 999_999)}_${counterRows.size}"
                                counterRows.add(CounterRow(id = newId, label = ""))
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.add),
                                contentDescription = "Add Row",
                                modifier = Modifier.size(16.dp),
                            )
                            Text("Add Item", modifier = Modifier.padding(start = 4.dp), fontFamily = flexFontRounded())
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 180.dp),
                    ) {
                        itemsIndexed(counterRows, key = { _, row -> row.id }) { index, row ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        FloatingLabelTextField(
                                            value = row.label,
                                            onValueChange = { newLabel ->
                                                counterRows[index] = row.copy(label = newLabel)
                                            },
                                            labelText = "Label",
                                            placeholderText = "e.g. Water Glass",
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                            modifier = Modifier.weight(1f),
                                        )

                                        IconButton(
                                            onClick = { counterRows.removeAt(index) },
                                            modifier = Modifier.padding(start = 4.dp),
                                        ) {
                                            Icon(
                                                imageVector = vectorResource(Res.drawable.close),
                                                contentDescription = "Delete Row",
                                                tint = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            FloatingLabelTextField(
                                                value = if (row.value == 0.0) "" else if (row.isInteger) row.value.toLong().toString() else row.value.toString(),
                                                onValueChange = { valStr ->
                                                    val parsed = valStr.toDoubleOrNull() ?: 0.0
                                                    counterRows[index] = row.copy(value = parsed)
                                                },
                                                labelText = "Value",
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f),
                                            )

                                            FloatingLabelTextField(
                                                value = row.unit ?: "",
                                                onValueChange = { u ->
                                                    counterRows[index] = row.copy(unit = u.ifBlank { null })
                                                },
                                                labelText = "Unit",
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                                modifier = Modifier.weight(1f),
                                            )
                                        }

                                        Spacer(modifier = Modifier.size(48.dp))
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
