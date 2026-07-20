package com.loc.hexis.shared.ui.note.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.note.Note
import com.loc.hexis.shared.ui.note.getContentPreview
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.archive
import hexis.shared.ui.generated.resources.delete
import hexis.shared.ui.generated.resources.flag
import hexis.shared.ui.generated.resources.unarchive
import hexis.shared.ui.generated.resources.untitled
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun BaseNoteCard(
    note: Note,
    showArchived: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onTogglePin: () -> Unit = {},
    onArchive: () -> Unit = {},
    onUnarchive: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
    body: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (showArchived) MaterialTheme.colorScheme.surfaceContainerLow
                    else MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = note.title.ifEmpty { stringResource(Res.string.untitled) },
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                if (note.pinned && !showArchived) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.flag),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp).padding(start = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            body()

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatNoteDate(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!showArchived) {
                        FilledTonalIconButton(
                            onClick = onTogglePin,
                            modifier = Modifier.size(32.dp),
                            colors =
                                IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor =
                                        if (note.pinned) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor =
                                        if (note.pinned)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            shapes =
                                IconButtonShapes(
                                    shape = RoundedCornerShape(10.dp),
                                    pressedShape = RoundedCornerShape(10.dp),
                                ),
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.flag),
                                contentDescription = if (note.pinned) "Unpin" else "Pin",
                                modifier = Modifier.size(14.dp),
                            )
                        }

                        FilledTonalIconButton(
                            onClick = onArchive,
                            modifier = Modifier.size(32.dp),
                            colors =
                                IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                            shapes =
                                IconButtonShapes(
                                    shape = RoundedCornerShape(10.dp),
                                    pressedShape = RoundedCornerShape(10.dp),
                                ),
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.archive),
                                contentDescription = "Archive",
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    } else {
                        IconButton(onClick = onUnarchive, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.unarchive),
                                contentDescription = "Unarchive Note",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.delete),
                                contentDescription = "Delete Note",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    showArchived: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onTogglePin: () -> Unit = {},
    onArchive: () -> Unit = {},
    onUnarchive: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
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
        if (note.content.isNotEmpty()) {
            Text(
                text = getContentPreview(note.content),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
