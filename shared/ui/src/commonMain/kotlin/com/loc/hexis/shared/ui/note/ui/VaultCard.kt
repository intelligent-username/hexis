/*
 * Copyright (C) 2024 Hexis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.loc.hexis.shared.ui.note.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.note.Note
import com.loc.hexis.shared.ui.note.getNoteColor
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.lock
import org.jetbrains.compose.resources.vectorResource

@Composable
fun VaultCard(
    note: Note,
    showArchived: Boolean = false,
    onClick: () -> Unit,
    onTogglePin: () -> Unit = {},
    onArchive: () -> Unit = {},
    onUnarchive: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val vaultData = remember(note.payloadJson) { note.parseVault() }
    val isDark = isSystemInDarkTheme()
    val customColor = getNoteColor(note.getColorHex(), isDark)
    val hasCustomColor = customColor != Color.Unspecified

    val onSurfaceColor = if (hasCustomColor) {
        if (customColor.luminance() < 0.5f) Color.White else Color.Black
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val onSurfaceVariantColor = if (hasCustomColor) {
        if (customColor.luminance() < 0.5f) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    BaseNoteCard(
        note = note,
        showArchived = showArchived,
        onClick = onClick,
        onTogglePin = onTogglePin,
        onArchive = onArchive,
        onUnarchive = onUnarchive,
        onDelete = onDelete,
        modifier = modifier,
    ) {
        // Lock icon + entry count row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.lock),
                contentDescription = null,
                tint = if (hasCustomColor) onSurfaceColor else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when (vaultData.entries.size) {
                    0 -> "No secrets"
                    1 -> "1 secret"
                    else -> "${vaultData.entries.size} secrets"
                },
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = flexFontRounded()),
                color = onSurfaceVariantColor
            )
        }

        // Label chips for first 3 entries
        if (vaultData.entries.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            val preview = remember(vaultData.entries) { vaultData.entries.take(3) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                preview.forEach { entry ->
                    val chipColor by animateColorAsState(
                        targetValue = if (hasCustomColor)
                            onSurfaceColor.copy(alpha = 0.12f)
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                    val chipTextColor by animateColorAsState(
                        targetValue = if (hasCustomColor)
                            onSurfaceColor
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = chipColor,
                    ) {
                        Text(
                            text = entry.label.ifBlank { "—" },
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded()),
                            color = chipTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
                if (vaultData.entries.size > 3) {
                    Text(
                        text = "+${vaultData.entries.size - 3}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded()),
                        color = onSurfaceVariantColor,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
            }
        }
    }
}
