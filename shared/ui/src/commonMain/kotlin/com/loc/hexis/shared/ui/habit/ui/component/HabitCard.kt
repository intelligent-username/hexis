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

package com.loc.hexis.shared.ui.habit.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.loc.hexis.core.habits.HabitWithAnalytics
import com.loc.hexis.core.habits.StreakPosition
import com.loc.hexis.core.habits.areConsecutiveEligibleDays
import com.loc.hexis.core.toFormattedString
import com.loc.hexis.shared.ui.habit.HabitsAction
import com.loc.hexis.shared.ui.util.rememberToday
import hexis.shared.ui.generated.resources.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.vectorResource

/** Habit Card for list */
@Composable
fun HabitCard(
    habitWithAnalytics: HabitWithAnalytics,
    completed: Boolean,
    action: (HabitsAction) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    editState: Boolean,
    compactView: Boolean,
    analyticsEnabled: Boolean,
    startingDay: DayOfWeek,
    reorderHandle: @Composable () -> Unit,
    is24Hr: Boolean,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val today by rememberToday()
    val canCompleteToday = today.dayOfWeek in habitWithAnalytics.habit.days

    // animated colors
    val cardContent by
        animateColorAsState(
            targetValue =
                when (completed) {
                    true -> MaterialTheme.colorScheme.onPrimaryContainer
                    else ->
                        MaterialTheme.colorScheme.onSurface.copy(
                            alpha = if (canCompleteToday) 1f else 0.7f
                        )
                },
            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
            label = "cardBackground",
        )
    val cardBackground by
        animateColorAsState(
            targetValue =
                when (completed) {
                    true -> MaterialTheme.colorScheme.primaryContainer
                    else ->
                        MaterialTheme.colorScheme.surfaceContainer.copy(
                            alpha = if (canCompleteToday) 1f else 0.7f
                        )
                },
            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
            label = "cardBackground",
        )

    val weekState =
        rememberWeekCalendarState(
            startDate = habitWithAnalytics.habit.time.date.minus(1, DateTimeUnit.YEAR),
            endDate = today,
            firstVisibleWeekDate = today,
            firstDayOfWeek = startingDay,
        )

    val interactionSource = remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 1.05f else 1f,
            animationSpec =
                if (isPressed) {
                    androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessHigh,
                    )
                } else {
                    androidx.compose.animation.core.spring(
                        dampingRatio =
                            androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow,
                    )
                },
            label = "scale",
        )

    Card(
        colors =
            CardDefaults.outlinedCardColors(
                containerColor = cardBackground,
                contentColor = cardContent,
            ),
        onClick = {
            if (canCompleteToday && !habitWithAnalytics.habit.pomodoroLinked) {
                action(HabitsAction.ToggleHabitProgress(habitWithAnalytics.habit, today))
            }
        },
        shape = shape,
        interactionSource = interactionSource,
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .animateContentSize(animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()),
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large),
            colors =
                ListItemDefaults.colors(
                    containerColor = cardBackground,
                    headlineColor = cardContent,
                    supportingColor = cardContent,
                    trailingIconColor = cardContent,
                    leadingIconColor = cardContent,
                ),
            leadingContent = {
                val displayMode = habitWithAnalytics.habit.displayMode
                if (displayMode == com.loc.hexis.core.habits.DisplayMode.PROGRESS) {
                    val currentValue =
                        habitWithAnalytics.habit.targetValue?.let { target ->
                            val status = habitWithAnalytics.statuses.find { it.date == today }
                            (status?.value ?: 0.0).coerceAtMost(target)
                        } ?: 0.0
                    val targetValue = habitWithAnalytics.habit.targetValue ?: 1.0
                    Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator(
                            progress = { (currentValue / targetValue).toFloat() },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 2.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color =
                                if (completed) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                        Text(
                            text = "${currentValue.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color =
                                if (completed) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                } else {
                    AnimatedContent(targetState = completed) {
                        Icon(
                            imageVector =
                                vectorResource(
                                    if (!it) Res.drawable.circle_border
                                    else Res.drawable.check_circle
                                ),
                            contentDescription = null,
                        )
                    }
                }
            },
            headlineContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = habitWithAnalytics.habit.title,
                        maxLines = 1,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.basicMarquee(),
                    )
                }
            },
            supportingContent = {
                Column {
                    if (!compactView && habitWithAnalytics.habit.description.isNotBlank()) {
                        Text(
                            text = habitWithAnalytics.habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            modifier = Modifier.padding(bottom = 2.dp),
                            color = cardContent.copy(alpha = 0.8f),
                        )
                    }
                    if (habitWithAnalytics.habit.reminder) {
                        Text(
                            text = habitWithAnalytics.habit.time.time.toFormattedString(is24Hr),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.heat),
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                        )

                        Text(text = habitWithAnalytics.currentStreak.toString())
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            action(HabitsAction.PrepareAnalytics(habitWithAnalytics.habit))
                            onNavigateToAnalytics()
                        },
                        modifier =
                            Modifier.size(
                                IconButtonDefaults.smallContainerSize(
                                    IconButtonDefaults.IconButtonWidthOption.Wide
                                )
                            ),
                        colors =
                            IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        enabled = analyticsEnabled,
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.analytics),
                            contentDescription = "Analytics",
                        )
                    }

                    AnimatedVisibility(visible = editState) {
                        Row {
                            Spacer(modifier = Modifier.width(8.dp))
                            reorderHandle()
                        }
                    }
                }
            },
        )

        if (!compactView) {
            WeekCalendar(
                contentPadding = PaddingValues(8.dp),
                state = weekState,
                dayContent = { weekDay ->
                    val startDate = habitWithAnalytics.habit.time.date
                    val target = habitWithAnalytics.habit.targetValue ?: 1.0
                    val done =
                        habitWithAnalytics.statuses.any {
                            it.date == weekDay.date && it.value >= target
                        }
                    val validDay =
                        weekDay.date <= today &&
                            weekDay.date >= startDate &&
                            weekDay.date.dayOfWeek in habitWithAnalytics.habit.days

                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                                .then(
                                    if (done) {
                                        val completedDates =
                                            remember(habitWithAnalytics.statuses, target) {
                                                habitWithAnalytics.statuses
                                                    .filter { it.value >= target }
                                                    .map { it.date }
                                                    .toSet()
                                            }
                                        val eligibleDays = habitWithAnalytics.habit.days

                                        val hasPreviousEligibleCompleted =
                                            completedDates.any { completedDate ->
                                                completedDate < weekDay.date &&
                                                    completedDate.dayOfWeek in eligibleDays &&
                                                    areConsecutiveEligibleDays(
                                                        completedDate,
                                                        weekDay.date,
                                                        eligibleDays,
                                                    )
                                            }

                                        val hasNextEligibleCompleted =
                                            completedDates.any { completedDate ->
                                                completedDate > weekDay.date &&
                                                    completedDate.dayOfWeek in eligibleDays &&
                                                    areConsecutiveEligibleDays(
                                                        weekDay.date,
                                                        completedDate,
                                                        eligibleDays,
                                                    )
                                            }

                                        val streakPosition: StreakPosition =
                                            when {
                                                hasPreviousEligibleCompleted &&
                                                    hasNextEligibleCompleted ->
                                                    StreakPosition.MIDDLE
                                                hasPreviousEligibleCompleted -> StreakPosition.END
                                                hasNextEligibleCompleted -> StreakPosition.START
                                                else -> StreakPosition.ISOLATED
                                            }

                                        val shape =
                                            when (streakPosition) {
                                                StreakPosition.ISOLATED -> RoundedCornerShape(20.dp)
                                                StreakPosition.START ->
                                                    RoundedCornerShape(
                                                        topStart = 20.dp,
                                                        bottomStart = 20.dp,
                                                    )
                                                StreakPosition.END ->
                                                    RoundedCornerShape(
                                                        topEnd = 20.dp,
                                                        bottomEnd = 20.dp,
                                                    )
                                                StreakPosition.MIDDLE -> RoundedCornerShape(0.dp)
                                            }

                                        Modifier.background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = shape,
                                        )
                                    } else Modifier
                                )
                                .then(
                                    if (weekDay.date == startDate && done) {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = Color(0xFFFFD700),
                                            shape = RoundedCornerShape(20.dp),
                                        )
                                    } else Modifier
                                )
                                .clip(shape = RoundedCornerShape(20.dp))
                                .clickable(
                                    role = Role.Button,
                                    enabled = validDay,
                                    onClick = {
                                        if (!habitWithAnalytics.habit.pomodoroLinked) {
                                            action(
                                                HabitsAction.ToggleHabitProgress(
                                                    habit = habitWithAnalytics.habit,
                                                    date = weekDay.date,
                                                )
                                            )
                                        }
                                    },
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = weekDay.date.day.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee(),
                                color =
                                    if (done) MaterialTheme.colorScheme.onPrimary
                                    else if (!validDay) cardContent.copy(alpha = 0.5f)
                                    else cardContent,
                            )

                            Text(
                                text = weekDay.date.dayOfWeek.toString().take(3),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee(),
                                color =
                                    if (done) MaterialTheme.colorScheme.onPrimary
                                    else if (!validDay) cardContent.copy(alpha = 0.5f)
                                    else cardContent,
                            )
                        }
                    }
                },
            )
        }
    }
}
