package com.loc.hexis.shared.ui.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.note.Note
import com.loc.hexis.core.toFormattedString
import com.loc.hexis.shared.ui.note.getNoteColor
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.calendar_month
import org.jetbrains.compose.resources.vectorResource

@Composable
fun JournalCard(
    note: Note,
    showArchived: Boolean = false,
    onClick: () -> Unit,
    onTogglePin: () -> Unit = {},
    onArchive: () -> Unit = {},
    onUnarchive: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val journalData = remember(note.payloadJson) { note.parseJournal() }
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.calendar_month),
                contentDescription = null,
                tint = if (hasCustomColor) onSurfaceColor else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${journalData.entries.size} logs",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = flexFontRounded()),
                color = onSurfaceVariantColor
            )
        }

        if (journalData.entries.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            // Show the last 3 entries
            val lastEntries = remember(journalData.entries) {
                journalData.entries.takeLast(3)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                lastEntries.forEach { entry ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if (hasCustomColor) onSurfaceColor else MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = buildString {
                                append(entry.timestamp.time.toFormattedString(is24Hr = false))
                                if (!entry.mood.isNullOrBlank()) {
                                    append(" ")
                                    append(entry.mood)
                                }
                                append(": ")
                                append(entry.text)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariantColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
