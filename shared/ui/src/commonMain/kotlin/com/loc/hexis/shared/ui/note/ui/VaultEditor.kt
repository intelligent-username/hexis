/*
 * Copyright (C) 2024-2026 Hexis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.loc.hexis.shared.ui.note.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loc.hexis.core.note.Note
import com.loc.hexis.core.note.VaultEntry
import com.loc.hexis.core.note.VaultNote
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.add
import hexis.shared.ui.generated.resources.check
import hexis.shared.ui.generated.resources.close
import hexis.shared.ui.generated.resources.content_copy
import hexis.shared.ui.generated.resources.delete
import hexis.shared.ui.generated.resources.lock
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.vectorResource
import kotlin.random.Random

@Composable
fun VaultEditor(
    note: Note,
    onSave: (Note) -> Unit,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    hasEditorCustomColor: Boolean,
) {
    val vaultData = remember(note.payloadJson) { note.parseVault() }
    val entries = remember(vaultData) {
        mutableStateListOf<VaultEntry>().apply { addAll(vaultData.entries) }
    }

    var newLabel by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var newNotes by remember { mutableStateOf("") }
    var showValueInForm by remember { mutableStateOf(false) }
    var showAddForm by remember { mutableStateOf(false) }

    // Id of entry whose value is currently revealed
    var revealedId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(revealedId) {
        if (revealedId != null) {
            delay(5000)
            revealedId = null
        }
    }

    fun save() {
        onSave(note.withVault(VaultNote(entries.toList())))
    }

    fun addEntry() {
        if (newLabel.isBlank() && newValue.isBlank()) return
        entries.add(
            VaultEntry(
                id = "vault_${Random.nextLong(100_000_000L, 999_999_999L)}_${entries.size}",
                label = newLabel.trim(),
                value = newValue.trim(),
                notes = newNotes.trim(),
            )
        )
        save()
        newLabel = ""
        newValue = ""
        newNotes = ""
        showValueInForm = false
        showAddForm = false
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = onSurfaceColor,
        unfocusedTextColor = onSurfaceColor,
        focusedBorderColor = if (hasEditorCustomColor) onSurfaceColor else MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = if (hasEditorCustomColor) onSurfaceVariantColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant,
        focusedPlaceholderColor = onSurfaceVariantColor,
        unfocusedPlaceholderColor = onSurfaceVariantColor,
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Secrets entry list
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.lock),
                                contentDescription = null,
                                tint = onSurfaceVariantColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No secrets stored yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                                color = onSurfaceColor
                            )
                            Text(
                                text = "Tap + to add your first secure credential or key",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = flexFontRounded()),
                                color = onSurfaceVariantColor.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    VaultEntryRow(
                        entry = entry,
                        isRevealed = revealedId == entry.id,
                        onReveal = { revealedId = if (revealedId == entry.id) null else entry.id },
                        onDelete = {
                            entries.removeAll { it.id == entry.id }
                            save()
                        },
                        onSurfaceColor = onSurfaceColor,
                        onSurfaceVariantColor = onSurfaceVariantColor,
                        hasEditorCustomColor = hasEditorCustomColor,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Add secret form bottom sheet / container
        AnimatedVisibility(
            visible = showAddForm,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            Surface(
                color = if (hasEditorCustomColor) onSurfaceColor.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add New Secret",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                            color = onSurfaceColor,
                        )
                        IconButton(onClick = { showAddForm = false }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.close),
                                contentDescription = "Close",
                                tint = onSurfaceVariantColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newLabel,
                        onValueChange = { newLabel = it },
                        placeholder = { Text("Label (e.g. WiFi Password, API Key)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                    )

                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        placeholder = { Text("Secret value") },
                        singleLine = true,
                        visualTransformation = if (showValueInForm) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        trailingIcon = {
                            IconButton(onClick = { showValueInForm = !showValueInForm }) {
                                Text(if (showValueInForm) "🙈" else "👁", fontSize = 16.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                    )

                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        placeholder = { Text("Notes (optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { showAddForm = false }) {
                            Text("Cancel", fontFamily = flexFontRounded())
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = ::addEntry,
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.check),
                                contentDescription = "Save Secret",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Secret", fontFamily = flexFontRounded())
                        }
                    }
                }
            }
        }

        // Bottom status bar + Add Secret FAB button
        if (!showAddForm) {
            Surface(
                color = if (hasEditorCustomColor) Color.Transparent
                else MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.lock),
                            contentDescription = null,
                            tint = if (hasEditorCustomColor) onSurfaceVariantColor else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (entries.size) {
                                0 -> "No secrets stored"
                                1 -> "1 secret stored"
                                else -> "${entries.size} secrets stored"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = flexFontRounded()),
                            color = onSurfaceVariantColor,
                        )
                    }
                    FilledTonalIconButton(
                        onClick = { showAddForm = true },
                        shape = CircleShape,
                        colors = if (hasEditorCustomColor)
                            IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = onSurfaceColor.copy(alpha = 0.15f),
                                contentColor = onSurfaceColor,
                            )
                        else
                            IconButtonDefaults.filledTonalIconButtonColors(),
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.add),
                            contentDescription = "Add secret",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultEntryRow(
    entry: VaultEntry,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onDelete: () -> Unit,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    hasEditorCustomColor: Boolean,
) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    val cardAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (hasEditorCustomColor)
            onSurfaceColor.copy(alpha = 0.08f)
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().alpha(cardAlpha),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = entry.label.ifBlank { "Untitled Secret" },
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                    color = onSurfaceColor,
                    modifier = Modifier.weight(1f),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Copy button
                    IconButton(
                        onClick = {
                            if (entry.value.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(entry.value))
                                isCopied = true
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) vectorResource(Res.drawable.check) else vectorResource(Res.drawable.content_copy),
                            contentDescription = "Copy Secret",
                            tint = if (isCopied) MaterialTheme.colorScheme.primary else onSurfaceVariantColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Reveal / hide toggle button
                    IconButton(onClick = onReveal, modifier = Modifier.size(32.dp)) {
                        Text(text = if (isRevealed) "🙈" else "👁", fontSize = 16.sp)
                    }

                    // Delete button
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.delete),
                            contentDescription = "Delete Secret",
                            tint = if (hasEditorCustomColor) onSurfaceVariantColor else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Masked or revealed secret value
            val displayValue = if (isRevealed) entry.value else "•".repeat(minOf(entry.value.length.coerceAtLeast(8), 16))
            val valueColor by animateColorAsState(
                targetValue = if (isRevealed) onSurfaceColor else onSurfaceVariantColor.copy(alpha = 0.6f)
            )

            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = if (isRevealed) null else FontFamily.Monospace,
                    letterSpacing = if (isRevealed) 0.sp else 2.sp,
                ),
                color = valueColor,
                modifier = Modifier.graphicsLayer {
                    scaleX = if (isRevealed) 1f else 0.96f
                    scaleY = if (isRevealed) 1f else 0.96f
                },
            )

            // Optional notes/description
            if (entry.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = onSurfaceVariantColor.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = flexFontRounded()),
                        color = onSurfaceVariantColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
