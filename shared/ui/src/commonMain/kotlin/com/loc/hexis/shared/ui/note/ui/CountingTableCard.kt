package com.loc.hexis.shared.ui.note.ui

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loc.hexis.core.note.CounterRow
import com.loc.hexis.core.note.Note
import com.loc.hexis.shared.ui.note.getNoteColor
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.add
import org.jetbrains.compose.resources.vectorResource

@Composable
fun CountingTableCard(
    note: Note,
    showArchived: Boolean = false,
    onClick: () -> Unit,
    onValueChange: (rowId: String, newValue: Double) -> Unit,
    onTogglePin: () -> Unit = {},
    onArchive: () -> Unit = {},
    onUnarchive: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tableData = remember(note.payloadJson) { note.parseCountingTable() }
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

    val primaryColor = if (hasCustomColor) {
        onSurfaceColor
    } else {
        MaterialTheme.colorScheme.primary
    }

    val dividerColor = if (hasCustomColor) {
        onSurfaceColor.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    }

    val pillColor = if (hasCustomColor) {
        onSurfaceColor.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
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
        if (tableData.rows.isEmpty()) {
            Text(
                text = "No counters added yet",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceVariantColor,
            )
        } else {
            tableData.rows.take(5).forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 5.dp),
                        color = dividerColor,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Flexible Item Label taking remaining space
                    Text(
                        text = row.label.ifBlank { "Item ${index + 1}" },
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariantColor,
                        modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )

                    // Counter Controls measuring exact required width
                    ScrollAndTypeCounter(
                        row = row,
                        onValueChange = { newValue -> onValueChange(row.id, newValue) },
                        contentColor = primaryColor,
                        pillColor = pillColor,
                        modifier = Modifier.padding(end = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun ScrollAndTypeCounter(
    row: CounterRow,
    onValueChange: (Double) -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    pillColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val dragThresholdPx = 24f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier,
    ) {
        // Compact Decrement (−) Button
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onValueChange((row.value - row.step).coerceAtLeast(0.0))
            },
            modifier = Modifier.size(20.dp),
        ) {
            Text(
                text = "−",
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = flexFontRounded(),
                        fontSize = 12.sp,
                    ),
                color = contentColor,
            )
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Center Value Pill
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = pillColor,
            modifier =
                Modifier.pointerInput(row.id, row.value) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulator -= dragAmount

                            if (dragAccumulator >= dragThresholdPx) {
                                val steps = (dragAccumulator / dragThresholdPx).toInt()
                                dragAccumulator %= dragThresholdPx
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onValueChange(row.value + (steps * row.step))
                            } else if (dragAccumulator <= -dragThresholdPx) {
                                val steps = (-dragAccumulator / dragThresholdPx).toInt()
                                dragAccumulator %= dragThresholdPx
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onValueChange((row.value - (steps * row.step)).coerceAtLeast(0.0))
                            }
                        }
                    )
                },
        ) {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            ) {
                val formattedVal =
                    if (row.isInteger) row.value.toLong().toString() else row.value.toString()
                Text(
                    text = if (!row.unit.isNullOrBlank()) "$formattedVal ${row.unit}" else formattedVal,
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = flexFontRounded(),
                            fontSize = 12.sp,
                        ),
                    color = contentColor,
                    textAlign = TextAlign.End,
                )
            }
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Compact Increment (+) Button
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onValueChange(row.value + row.step)
            },
            modifier = Modifier.size(20.dp),
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.add),
                contentDescription = "Increment",
                tint = contentColor,
                modifier = Modifier.size(10.dp),
            )
        }
    }
}
