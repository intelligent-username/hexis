package com.loc.hexis.shared.ui.note.ui

import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loc.hexis.core.note.CounterRow
import com.loc.hexis.core.note.Note
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.add
import org.jetbrains.compose.resources.vectorResource

@Composable
fun CountingTableCard(
    note: Note,
    showArchived: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onValueChange: (rowId: String, newValue: Double) -> Unit,
    onTogglePin: () -> Unit = {},
    onArchive: () -> Unit = {},
    onUnarchive: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tableData = remember(note.payloadJson) { note.parseCountingTable() }

    BaseNoteCard(
        note = note,
        showArchived = showArchived,
        onClick = onClick,
        onLongClick = onLongClick,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            tableData.rows.take(5).forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 5.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )

                    // Counter Controls measuring exact required width
                    ScrollAndTypeCounter(
                        row = row,
                        onValueChange = { newValue -> onValueChange(row.id, newValue) },
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
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Center Value Pill
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                    color = MaterialTheme.colorScheme.primary,
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(10.dp),
            )
        }
    }
}
