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

package com.loc.hexis.shared.ui.task.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.interfaces.ThemeDatastore
import com.loc.hexis.core.tasks.PomodoroSettings
import com.loc.hexis.shared.ui.app.FullScreenMode
import com.loc.hexis.shared.ui.app.KeepScreenOn
import com.loc.hexis.shared.ui.app.SystemBackHandler
import com.loc.hexis.shared.ui.components.HexisBottomSheet
import com.loc.hexis.shared.ui.task.PomodoroManager
import com.loc.hexis.shared.ui.task.PomodoroPhase
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.chart_data
import hexis.shared.ui.generated.resources.close
import hexis.shared.ui.generated.resources.edit
import hexis.shared.ui.generated.resources.pause
import hexis.shared.ui.generated.resources.play_arrow
import hexis.shared.ui.generated.resources.restart
import hexis.shared.ui.generated.resources.skip
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject

@Composable
fun PomodoroPage(linkedHabitId: Long? = null, onDismiss: () -> Unit) {
    val themeDatastore: ThemeDatastore = koinInject()
    val isAmoled by themeDatastore.getAmoledPref().collectAsStateWithLifecycle(initialValue = false)

    val pomodoroManager: PomodoroManager = koinInject()
    val pomodoroState by pomodoroManager.state.collectAsStateWithLifecycle()

    val settings = pomodoroState.settings
    val phase = pomodoroState.phase
    val secondsRemaining = pomodoroState.secondsRemaining
    val isRunning = pomodoroState.isRunning
    val currentSessionInBatch = pomodoroState.currentSessionInBatch
    val todayStats = pomodoroState.todayStats

    var showSettings by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }
    var focusText by remember { mutableStateOf("") }
    var shortBreakText by remember { mutableStateOf("") }
    var longBreakText by remember { mutableStateOf("") }
    var intervalText by remember { mutableStateOf("") }

    var currentHabitId by remember(linkedHabitId, pomodoroState.linkedHabitId) {
        mutableStateOf(linkedHabitId ?: pomodoroState.linkedHabitId)
    }
    var habitTitle by remember { mutableStateOf("") }

    val habitRepo: HabitRepo = koinInject()

    LaunchedEffect(currentHabitId) {
        if (currentHabitId != null) {
            val h = habitRepo.getHabitById(currentHabitId!!)
            if (h != null) habitTitle = h.title
        } else {
            habitTitle = ""
        }
    }

    fun formatMinutes(mins: Float): String =
        if (mins == mins.toInt().toFloat()) mins.toInt().toString() else mins.toString()

    fun applyPomodoroSettings() {
        val ns =
            PomodoroSettings(
                focusMinutes = focusText.toFloatOrNull() ?: settings.focusMinutes,
                shortBreakMinutes = shortBreakText.toFloatOrNull() ?: settings.shortBreakMinutes,
                longBreakMinutes = longBreakText.toFloatOrNull() ?: settings.longBreakMinutes,
                longBreakInterval = intervalText.toIntOrNull() ?: settings.longBreakInterval,
            )
        pomodoroManager.applyPomodoroSettings(ns)
        showSettings = false
    }

    fun handleDismiss() {
        onDismiss()
    }

    SystemBackHandler(enabled = true) {
        if (showAnalytics) {
            showAnalytics = false
        } else {
            handleDismiss()
        }
    }

    FullScreenMode(enabled = isRunning)
    KeepScreenOn(enabled = isRunning)

    LaunchedEffect(showSettings) {
        if (showSettings) {
            focusText = formatMinutes(settings.focusMinutes)
            shortBreakText = formatMinutes(settings.shortBreakMinutes)
            longBreakText = formatMinutes(settings.longBreakMinutes)
            intervalText = settings.longBreakInterval.toString()
        }
    }

    val totalSeconds = pomodoroState.durationSeconds

    val progress by
        animateFloatAsState(
            targetValue =
                if (totalSeconds > 0) 1f - secondsRemaining.toFloat() / totalSeconds else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "progress",
        )

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val breakColor = if (phase != PomodoroPhase.FOCUS) tertiary else primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerHi = MaterialTheme.colorScheme.surfaceContainerHighest
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    val backgroundColor by
        animateColorAsState(
            targetValue = if (isRunning || isAmoled) Color.Black else surface,
            animationSpec = tween(300),
            label = "backgroundColor",
        )
    val timerTextColor by
        animateColorAsState(
            targetValue = if (isRunning || isAmoled) Color.White else onSurface,
            animationSpec = tween(300),
            label = "timerTextColor",
        )

    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(backgroundColor)
                .pointerInput(isRunning) {
                    if (isRunning) {
                        detectTapGestures { pomodoroManager.pauseSession() }
                    }
                }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // top bar
            AnimatedVisibility(
                visible = !isRunning,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(250)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = { handleDismiss() }) {
                        Icon(
                            vectorResource(Res.drawable.close),
                            contentDescription = "Close",
                            tint = onSurfaceVariant,
                        )
                    }

                    if (habitTitle.isNotEmpty()) {
                        Text(
                            text = habitTitle,
                            fontFamily = flexFontRounded(),
                            style = MaterialTheme.typography.titleMedium,
                            color = onSurface,
                            maxLines = 1,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        todayStats?.let { stats ->
                            val tenths = (stats.totalMinutes * 10).toInt()
                            val whole = tenths / 10
                            val frac = tenths % 10
                            val display = if (frac == 0) "${whole}m" else "$whole.${frac}m"
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = breakColor.copy(alpha = 0.12f),
                            ) {
                                Text(
                                    text = "${stats.sessionCount}  \u00B7  $display",
                                    fontFamily = flexFontRounded(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = breakColor,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }
                        }

                        FilledTonalIconButton(
                            onClick = { showAnalytics = true },
                            modifier = Modifier.size(36.dp),
                            colors =
                                IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = surfaceContainerHigh,
                                    contentColor = onSurfaceVariant,
                                ),
                            shapes =
                                IconButtonShapes(
                                    shape = CircleShape,
                                    pressedShape = MaterialTheme.shapes.small,
                                ),
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.chart_data),
                                contentDescription = "Session History",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(0.4f))

            // timer arc
            Box(modifier = Modifier.size(336.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(336.dp)) {
                    val sw = 8.dp.toPx()
                    val arcS = Size(size.width - sw, size.height - sw)
                    val tl = Offset(sw / 2f, sw / 2f)

                    drawArc(
                        color = breakColor.copy(alpha = 0.07f),
                        startAngle = -90f,
                        sweepAngle = (progress * 360f + 6f).coerceAtMost(366f),
                        useCenter = false,
                        topLeft = Offset(sw / 2f - 8.dp.toPx(), sw / 2f - 8.dp.toPx()),
                        size = Size(arcS.width + 16.dp.toPx(), arcS.height + 16.dp.toPx()),
                        style = Stroke(width = sw + 16.dp.toPx(), cap = StrokeCap.Round),
                    )

                    drawArc(
                        color = if (isRunning) Color.DarkGray.copy(alpha = 0.3f) else surfaceContainerHi,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = tl,
                        size = arcS,
                        style = Stroke(width = sw, cap = StrokeCap.Round),
                    )

                    drawArc(
                        color = breakColor,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        topLeft = tl,
                        size = arcS,
                        style = Stroke(width = sw, cap = StrokeCap.Round),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text =
                            "${secondsRemaining / 60}:${(secondsRemaining % 60).toString().padStart(2, '0')}",
                        fontFamily = flexFontRounded(),
                        fontSize = 96.sp,
                        fontWeight = FontWeight.Bold,
                        color = timerTextColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // cycle counter — shows current session number (1 to interval)
            AnimatedVisibility(
                visible = !isRunning && settings.longBreakInterval > 0,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(250)),
            ) {
                Text(
                    text = "$currentSessionInBatch / ${settings.longBreakInterval} cycles",
                    fontFamily = flexFontRounded(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            Spacer(Modifier.weight(0.2f))

            // controls
            AnimatedVisibility(
                visible = !isRunning,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(250)),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // reset
                    FilledTonalIconButton(
                        onClick = { pomodoroManager.resetTimer() },
                        modifier = Modifier.size(48.dp),
                        colors =
                            IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = surfaceContainerHigh,
                                contentColor = onSurfaceVariant,
                            ),
                        shapes =
                            IconButtonShapes(
                                shape = CircleShape,
                                pressedShape = MaterialTheme.shapes.small,
                            ),
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.restart),
                            contentDescription = "Reset",
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    // start / pause
                    Box(
                        modifier =
                            Modifier.size(80.dp).background(breakColor, CircleShape).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                if (isRunning) {
                                    pomodoroManager.pauseSession()
                                } else if (
                                    pomodoroState.currentSessionId == null &&
                                        phase == PomodoroPhase.FOCUS
                                ) {
                                    pomodoroManager.startSession(currentHabitId)
                                } else {
                                    pomodoroManager.resumeSession(currentHabitId)
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector =
                                vectorResource(
                                    if (isRunning) Res.drawable.pause else Res.drawable.play_arrow
                                ),
                            contentDescription = if (isRunning) "Pause" else "Start",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(36.dp),
                        )
                    }

                    // settings / skip
                    if (phase != PomodoroPhase.FOCUS) {
                        FilledTonalIconButton(
                            onClick = { pomodoroManager.skipBreak() },
                            modifier = Modifier.size(48.dp),
                            colors =
                                IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = surfaceContainerHigh,
                                    contentColor = onSurfaceVariant,
                                ),
                            shapes =
                                IconButtonShapes(
                                    shape = CircleShape,
                                    pressedShape = MaterialTheme.shapes.small,
                                ),
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.skip),
                                contentDescription = "Skip",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    } else {
                        FilledTonalIconButton(
                            onClick = { showSettings = true },
                            modifier = Modifier.size(48.dp),
                            colors =
                                IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = surfaceContainerHigh,
                                    contentColor = onSurfaceVariant,
                                ),
                            shapes =
                                IconButtonShapes(
                                    shape = CircleShape,
                                    pressedShape = MaterialTheme.shapes.small,
                                ),
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.edit),
                                contentDescription = "Settings",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(0.4f))
        }
    }

    // --- settings bottom sheet ---

    if (showSettings) {
        HexisBottomSheet(onDismissRequest = { showSettings = false }, padding = 0.dp) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                item { SettingsField("Work", focusText, { focusText = it }, "min") }
                item {
                    SettingsField("Short break", shortBreakText, { shortBreakText = it }, "min")
                }
                item { SettingsField("Long break", longBreakText, { longBreakText = it }, "min") }
                item {
                    SettingsField(
                        "Long break every",
                        intervalText,
                        { intervalText = it },
                        "focuses",
                    )
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(
                            onClick = { applyPomodoroSettings() },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                            shapes =
                                ButtonShapes(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    pressedShape = MaterialTheme.shapes.small,
                                ),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                        ) {
                            Text("Save", fontFamily = flexFontRounded())
                        }
                    }
                }
            }
        }
    }

    if (showAnalytics) {
        PomodoroAnalytics(
            onDismiss = { showAnalytics = false },
            onSelectHabit = { selectedId, selectedTitle ->
                showAnalytics = false
                currentHabitId = selectedId
                habitTitle = selectedTitle
                pomodoroManager.setLinkedHabit(selectedId)
            },
        )
    }
}

@Composable
private fun SettingsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontFamily = flexFontRounded(), style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(100.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.End),
            suffix = { Text(suffix, style = MaterialTheme.typography.bodySmall) },
            shape = MaterialTheme.shapes.medium,
        )
    }
}
