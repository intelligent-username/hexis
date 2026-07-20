package com.loc.hexis.shared.ui.task.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loc.hexis.core.tasks.Task
import com.loc.hexis.core.toFormattedString
import hexis.shared.ui.generated.resources.*
import org.jetbrains.compose.resources.vectorResource

@Composable
fun TaskCard(
    task: Task,
    dragState: Boolean = false,
    reorderIcon: @Composable () -> Unit,
    is24Hr: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    val cardContent by
        animateColorAsState(
            targetValue =
                when (task.status) {
                    true -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                },
            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
            label = "cardContent",
        )
    val cardContainer by
        animateColorAsState(
            targetValue =
                when (task.status) {
                    true -> MaterialTheme.colorScheme.surfaceContainerHighest
                    else -> MaterialTheme.colorScheme.secondaryContainer
                },
            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
            label = "cardContainer",
        )
    val cardColors =
        CardDefaults.cardColors(containerColor = cardContainer, contentColor = cardContent)

    Card(
        modifier =
            modifier.animateContentSize(
                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
            ),
        colors = cardColors,
        shape = shape,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    textDecoration =
                        if (task.status) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                )

                if (task.reminder != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.alarm),
                            contentDescription = "Reminder",
                            modifier = Modifier.size(12.dp),
                        )

                        Text(
                            text = task.reminder!!.toFormattedString(is24Hr),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light,
                                ),
                        )
                    }
                }
            }

            AnimatedVisibility(visible = dragState, enter = fadeIn(), exit = fadeOut()) {
                reorderIcon()
            }
        }
    }
}
