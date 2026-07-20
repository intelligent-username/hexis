package com.loc.hexis.shared.ui.note.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loc.hexis.core.note.Note
import com.loc.hexis.core.now
import com.loc.hexis.shared.ui.note.getNextListPrefix
import com.loc.hexis.shared.ui.note.removePrefix
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.close
import hexis.shared.ui.generated.resources.edit_note
import hexis.shared.ui.generated.resources.new_note
import hexis.shared.ui.generated.resources.save
import hexis.shared.ui.generated.resources.title
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun NoteEditorSheet(note: Note?, onDismissRequest: () -> Unit, onSave: (Note) -> Unit) {
    var title by remember(note) { mutableStateOf(note?.title ?: "") }
    var contentValue by remember(note) {
        mutableStateOf(
            TextFieldValue((note?.content ?: "").replace("\r\n", "\n").replace('\r', '\n'))
        )
    }

    val wordCount = remember(contentValue.text) {
        if (contentValue.text.isBlank()) 0
        else contentValue.text.trim().split(Regex("""\s+""")).size
    }
    val charCount = remember(contentValue.text) { contentValue.text.length }

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismissRequest) {
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
                    Text(
                        text = "$wordCount words • $charCount chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Button(
                    onClick = {
                        val now = LocalDateTime.now()
                        onSave(
                            (note
                                    ?: Note(
                                        title = "",
                                        content = "",
                                        createdAt = now,
                                        updatedAt = now,
                                    ))
                                .copy(title = title, content = contentValue.text, updatedAt = now)
                        )
                    },
                    shapes = ButtonShapes(shape = CircleShape, pressedShape = CircleShape),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Text(stringResource(Res.string.save), fontFamily = flexFontRounded())
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                shape = RoundedCornerShape(16.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Editor Field
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
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
                    visualTransformation =
                        NoteVisualTransformation(
                            primaryColor = MaterialTheme.colorScheme.primary,
                            mutedColor = MaterialTheme.colorScheme.outline,
                            ruleColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    placeholder = {
                        Text("Start writing\u2026", style = MaterialTheme.typography.bodyLarge)
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
                    modifier =
                        Modifier.fillMaxSize()
                            .onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                                    val text = contentValue.text
                                    val cursor = contentValue.selection.start
                                    val currentLine = getCurrentLine(text, cursor)
                                    val prefix = getNextListPrefix(currentLine)
                                    if (prefix != null) {
                                        val before = text.substring(0, cursor.coerceAtMost(text.length))
                                        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it + 1 }
                                        val after = text.substring(lineEnd)
                                        val cleanLine = removePrefix(currentLine).trim()
                                        if (cleanLine.isEmpty()) {
                                            val lineStart = before.lastIndexOf('\n') + 1
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
                            },
                    shape = RoundedCornerShape(20.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                )
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
        }
    }
}
