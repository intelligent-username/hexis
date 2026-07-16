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

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    var contentValue by remember(note) { mutableStateOf(TextFieldValue(note?.content ?: "")) }

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

    fun applyFormat(prefix: String) {
        val text = contentValue.text
        val cursor = contentValue.selection.start
        val currentLine = getCurrentLine(text, cursor)
        val cleanLine = removePrefix(currentLine)
        val newLine = "$prefix$cleanLine"
        val newText = replaceCurrentLine(text, cursor, newLine)
        val newCursor = (newText.indexOf(newLine) + newLine.length).coerceAtMost(newText.length)
        contentValue = TextFieldValue(newText, TextRange(newCursor))
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.close),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text =
                        if (note == null) stringResource(Res.string.new_note)
                        else stringResource(Res.string.edit_note),
                    style =
                        MaterialTheme.typography.titleLarge.copy(fontFamily = flexFontEmphasis()),
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )

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
                    shapes =
                        ButtonShapes(
                            shape = MaterialTheme.shapes.large,
                            pressedShape = MaterialTheme.shapes.small,
                        ),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                ) {
                    Text(stringResource(Res.string.save), fontFamily = flexFontRounded())
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { newValue ->
                    title =
                        newValue.split(" ").joinToString(" ") { word ->
                            word.replaceFirstChar {
                                if (it.isLowerCase()) it.uppercase() else it.toString()
                            }
                        }
                },
                label = { Text(stringResource(Res.string.title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.large,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Content — WYSIWYG, markdown-free
            OutlinedTextField(
                value = contentValue,
                onValueChange = { contentValue = it },
                visualTransformation = NoteVisualTransformation(),
                placeholder = {
                    Text("Start writing\u2026", style = MaterialTheme.typography.bodyLarge)
                },
                modifier =
                    Modifier.fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyUp && event.key == Key.Enter) {
                                val text = contentValue.text
                                val cursor = contentValue.selection.start
                                val currentLine = getCurrentLine(text, cursor)
                                val prefix = getNextListPrefix(currentLine)
                                if (prefix != null) {
                                    val before = text.substring(0, cursor)
                                    val after = text.substring(cursor)
                                    val cleanLine = removePrefix(currentLine).trim()
                                    if (cleanLine.isEmpty()) {
                                        val lineStart = before.lastIndexOf('\n') + 1
                                        val newText = text.substring(0, lineStart) + "\n" + after
                                        contentValue =
                                            TextFieldValue(newText, TextRange(lineStart + 1))
                                    } else {
                                        val newText = before + "\n" + prefix + after
                                        val newCursor = before.length + 1 + prefix.length
                                        contentValue = TextFieldValue(newText, TextRange(newCursor))
                                    }
                                    true
                                } else false
                            } else false
                        },
                shape = MaterialTheme.shapes.large,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
            )

            // Toolbar
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(
                        "H1" to { applyFormat("# ") },
                        "H2" to { applyFormat("## ") },
                        "H3" to { applyFormat("### ") },
                    )
                    .forEach { (label, onClick) ->
                        FilledTonalButton(
                            onClick = onClick,
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        ) {
                            Text(
                                text = label,
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = flexFontRounded()
                                    ),
                            )
                        }
                    }
            }
        }
    }
}
