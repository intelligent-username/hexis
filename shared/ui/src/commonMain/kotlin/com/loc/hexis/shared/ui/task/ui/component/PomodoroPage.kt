package com.loc.hexis.shared.ui.task.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.loc.hexis.shared.ui.app.SystemBackHandler
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.loc.hexis.core.interfaces.PomodoroAlarm
import com.loc.hexis.core.interfaces.VibratorUtil
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loc.hexis.core.now
import com.loc.hexis.core.habits.HabitRepo
import com.loc.hexis.core.tasks.PomodoroRepo
import com.loc.hexis.core.tasks.PomodoroSession
import com.loc.hexis.core.tasks.PomodoroSettings
import com.loc.hexis.core.tasks.PomodoroStats
import com.loc.hexis.shared.ui.components.HexisBottomSheet
import com.loc.hexis.shared.ui.theme.flexFontRounded
import hexis.shared.ui.generated.resources.Res
import hexis.shared.ui.generated.resources.chart_data
import hexis.shared.ui.generated.resources.close
import hexis.shared.ui.generated.resources.edit
import hexis.shared.ui.generated.resources.pause
import hexis.shared.ui.generated.resources.play_arrow
import hexis.shared.ui.generated.resources.restart
import hexis.shared.ui.generated.resources.skip
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject

private enum class PomodoroPhase { FOCUS, SHORT_BREAK, LONG_BREAK }

@Composable
fun PomodoroPage(linkedHabitId: Long? = null, onDismiss: () -> Unit) {
    val repo: PomodoroRepo = koinInject()
    val settingsDatastore: com.loc.hexis.core.interfaces.SettingsDatastore = koinInject()

    var settings by remember { mutableStateOf(PomodoroSettings()) }
    var phase by remember { mutableStateOf(PomodoroPhase.FOCUS) }
    var secondsRemaining by remember { mutableStateOf((settings.focusMinutes * 60).toInt()) }
    var isRunning by remember { mutableStateOf(false) }
    var cyclesCompleted by remember { mutableStateOf(0) }
    var currentSessionInBatch by remember { mutableStateOf(1) }
    var currentSessionId by remember { mutableStateOf<Long?>(null) }
    var sessionStartTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var transitionCountdown by remember { mutableStateOf(0) }
    var todayStats by remember { mutableStateOf<PomodoroStats?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }

    var focusText by remember { mutableStateOf("") }
    var shortBreakText by remember { mutableStateOf("") }
    var longBreakText by remember { mutableStateOf("") }
    var intervalText by remember { mutableStateOf("") }
    var habitTitle by remember { mutableStateOf("") }

    val habitRepo: HabitRepo = koinInject()

    LaunchedEffect(linkedHabitId) {
        habitTitle = ""
        if (linkedHabitId != null) {
            val h = habitRepo.getHabitById(linkedHabitId); if (h != null) habitTitle = h.title
        }
    }

    val scope = rememberCoroutineScope()
    val vibrator: VibratorUtil = koinInject()
    val pomodoroAlarm: PomodoroAlarm = koinInject()

    fun formatMinutes(mins: Float): String =
        if (mins == mins.toInt().toFloat()) mins.toInt().toString() else mins.toString()

    // --- helpers (defined before LaunchedEffects that use them) ---

    fun savePartialSession() {
        val nw = LocalDateTime.now()
        val id = currentSessionId
        val start = sessionStartTime
        if (id != null && start != null) {
            val elapsed = (nw.toInstant(TimeZone.currentSystemDefault()).epochSeconds -
                start.toInstant(TimeZone.currentSystemDefault()).epochSeconds) / 60f
            scope.launch {
                repo.finishSession(id, nw, false, elapsed)
                todayStats = repo.getTodayStats()
            }
        }
        currentSessionId = null
        sessionStartTime = null
    }

    fun startSession() {
        savePartialSession()
        val nw = LocalDateTime.now()
        sessionStartTime = nw
        scope.launch {
            currentSessionId = repo.insertSession(
                PomodoroSession(goalDurationMinutes = settings.focusMinutes.toInt(), timeStarted = nw, linkedHabitId = linkedHabitId)
            )
        }
        secondsRemaining = (settings.focusMinutes * 60).toInt()
        phase = PomodoroPhase.FOCUS
        isRunning = true
        pomodoroAlarm.schedule(LocalDateTime.now().toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds() + secondsRemaining * 1000L)
    }

    fun resetTimer() {
        pomodoroAlarm.cancel()
        savePartialSession()
        isRunning = false
        when (phase) {
            PomodoroPhase.FOCUS -> secondsRemaining = (settings.focusMinutes * 60).toInt()
            PomodoroPhase.SHORT_BREAK -> secondsRemaining = (settings.shortBreakMinutes * 60).toInt()
            PomodoroPhase.LONG_BREAK -> secondsRemaining = (settings.longBreakMinutes * 60).toInt()
        }
    }

    fun onPhaseComplete() {
        pomodoroAlarm.cancel()
        isRunning = false
        when (phase) {
            PomodoroPhase.FOCUS -> {
                vibrator.buzz()
                val nw = LocalDateTime.now()
                val id = currentSessionId
                val start = sessionStartTime
                val elapsed = start?.let { s ->
                    val diff = nw.toInstant(TimeZone.currentSystemDefault()).epochSeconds -
                        s.toInstant(TimeZone.currentSystemDefault()).epochSeconds
                    (diff / 60f).coerceAtMost(settings.focusMinutes)
                } ?: settings.focusMinutes
                scope.launch {
                    id?.let { repo.finishSession(it, nw, true, elapsed) }
                    todayStats = repo.getTodayStats()
                    val hId = linkedHabitId
                    if (hId != null) {
                        val habit = habitRepo.getHabitById(hId)
                        if (habit != null) {
                            habitRepo.incrementHabitProgress(hId, LocalDate.now(), habit.incrementBy)
                        }
                    }
                }
                cyclesCompleted++
                if (settings.longBreakInterval > 0 && currentSessionInBatch >= settings.longBreakInterval) {
                    currentSessionInBatch = 1
                    phase = PomodoroPhase.LONG_BREAK
                    secondsRemaining = (settings.longBreakMinutes * 60).toInt()
                } else {
                    currentSessionInBatch++
                    phase = PomodoroPhase.SHORT_BREAK
                    secondsRemaining = (settings.shortBreakMinutes * 60).toInt()
                }
                currentSessionId = null
                sessionStartTime = null
                transitionCountdown = 2
                scope.launch {
                    delay(2000L)
                    if (!isRunning && phase != PomodoroPhase.FOCUS) isRunning = true
                }
            }
            PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK -> {
                vibrator.buzz()
                if (phase == PomodoroPhase.LONG_BREAK) cyclesCompleted = 0
                phase = PomodoroPhase.FOCUS
                secondsRemaining = (settings.focusMinutes * 60).toInt()
                sessionStartTime = null
                currentSessionId = null
                transitionCountdown = 2
                scope.launch {
                    delay(2000L)
                    if (!isRunning && phase == PomodoroPhase.FOCUS) {
                        val nw2 = LocalDateTime.now()
                        sessionStartTime = nw2
                        currentSessionId = repo.insertSession(
                            PomodoroSession(goalDurationMinutes = settings.focusMinutes.toInt(), timeStarted = nw2, linkedHabitId = linkedHabitId)
                        )
                        isRunning = true
                        pomodoroAlarm.schedule(LocalDateTime.now().toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds() + secondsRemaining * 1000L)
                    }
                }
            }
        }
    }

    fun skipBreak() {
        pomodoroAlarm.cancel()
        isRunning = false
        phase = PomodoroPhase.FOCUS
        secondsRemaining = (settings.focusMinutes * 60).toInt()
    }

    fun applyPomodoroSettings() {
        val ns = PomodoroSettings(
            focusMinutes = focusText.toFloatOrNull() ?: settings.focusMinutes,
            shortBreakMinutes = shortBreakText.toFloatOrNull() ?: settings.shortBreakMinutes,
            longBreakMinutes = longBreakText.toFloatOrNull() ?: settings.longBreakMinutes,
            longBreakInterval = intervalText.toIntOrNull() ?: settings.longBreakInterval,
        )
        scope.launch { settingsDatastore.setPomodoroSettings(ns) }
        settings = ns
        if (!isRunning && phase == PomodoroPhase.FOCUS) {
            secondsRemaining = (ns.focusMinutes * 60).toInt()
        }
        showSettings = false
    }

    fun handleDismiss() {
        savePartialSession()
        onDismiss()
    }

    SystemBackHandler(enabled = true) {
        if (showAnalytics) {
            showAnalytics = false
        } else {
            handleDismiss()
        }
    }

    // --- LaunchedEffects ---

    LaunchedEffect(Unit) {
        val loaded = settingsDatastore.getPomodoroSettings().first()
        settings = loaded
        secondsRemaining = (loaded.focusMinutes * 60).toInt()
    }

    LaunchedEffect(Unit) {
        while (true) {
            todayStats = repo.getTodayStats()
            delay(5000)
        }
    }

    LaunchedEffect(showSettings) {
        if (showSettings) {
            focusText = formatMinutes(settings.focusMinutes)
            shortBreakText = formatMinutes(settings.shortBreakMinutes)
            longBreakText = formatMinutes(settings.longBreakMinutes)
            intervalText = settings.longBreakInterval.toString()
        }
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (secondsRemaining > 0) {
                delay(1000L)
                secondsRemaining--
            }
            onPhaseComplete()
        }
    }

    LaunchedEffect(transitionCountdown) {
        if (transitionCountdown > 0) {
            delay(1000L)
            transitionCountdown--
        }
    }

    val totalSeconds = when (phase) {
        PomodoroPhase.FOCUS -> (settings.focusMinutes * 60).toInt()
        PomodoroPhase.SHORT_BREAK -> (settings.shortBreakMinutes * 60).toInt()
        PomodoroPhase.LONG_BREAK -> (settings.longBreakMinutes * 60).toInt()
    }

    val progress by animateFloatAsState(
        targetValue = if (totalSeconds > 0) 1f - secondsRemaining.toFloat() / totalSeconds else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "progress",
    )

    // --- colors ---

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val breakColor = if (phase != PomodoroPhase.FOCUS) tertiary else primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerHi = MaterialTheme.colorScheme.surfaceContainerHighest
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    // --- UI ---

    Box(modifier = Modifier.fillMaxSize().background(surface)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { handleDismiss() }) {
                    Icon(vectorResource(Res.drawable.close), contentDescription = "Close", tint = onSurfaceVariant)
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

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    todayStats?.let { stats ->
                        val tenths = (stats.totalMinutes * 10).toInt()
                        val whole = tenths / 10
                        val frac = tenths % 10
                        val display = if (frac == 0) "${whole}m" else "$whole.${frac}m"
                        Surface(shape = RoundedCornerShape(20.dp), color = breakColor.copy(alpha = 0.12f)) {
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
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = surfaceContainerHigh,
                            contentColor = onSurfaceVariant,
                        ),
                        shapes = IconButtonShapes(shape = CircleShape, pressedShape = MaterialTheme.shapes.small),
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.chart_data),
                            contentDescription = "Session History",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.weight(0.4f))

            // timer arc
            Box(
                modifier = Modifier.size(336.dp),
                contentAlignment = Alignment.Center,
            ) {
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
                        color = surfaceContainerHi,
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
                        text = "${secondsRemaining / 60}:${(secondsRemaining % 60).toString().padStart(2, '0')}",
                        fontFamily = flexFontRounded(),
                        fontSize = 96.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurface,
                        textAlign = TextAlign.Center,
                    )

                    AnimatedVisibility(visible = transitionCountdown > 0) {
                        Text(
                            text = "Starting in $transitionCountdown...",
                            fontFamily = flexFontRounded(),
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            // cycle counter — shows current session number (1 to interval)
            if (settings.longBreakInterval > 0) {
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // reset
                FilledTonalIconButton(
                    onClick = { resetTimer() },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = surfaceContainerHigh,
                        contentColor = onSurfaceVariant,
                    ),
                    shapes = IconButtonShapes(shape = CircleShape, pressedShape = MaterialTheme.shapes.small),
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.restart),
                        contentDescription = "Reset",
                        modifier = Modifier.size(20.dp),
                    )
                }

                // start / pause
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(breakColor, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (isRunning) {
                                pomodoroAlarm.cancel()
                                isRunning = false
                            } else if (currentSessionId == null && phase == PomodoroPhase.FOCUS) {
                                startSession()
                            } else {
                                isRunning = true
                                pomodoroAlarm.schedule(LocalDateTime.now().toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds() + secondsRemaining * 1000L)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = vectorResource(if (isRunning) Res.drawable.pause else Res.drawable.play_arrow),
                        contentDescription = if (isRunning) "Pause" else "Start",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp),
                    )
                }

                // settings / skip
                if (phase != PomodoroPhase.FOCUS) {
                    FilledTonalIconButton(
                        onClick = { skipBreak() },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = surfaceContainerHigh,
                            contentColor = onSurfaceVariant,
                        ),
                        shapes = IconButtonShapes(shape = CircleShape, pressedShape = MaterialTheme.shapes.small),
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
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = surfaceContainerHigh,
                            contentColor = onSurfaceVariant,
                        ),
                        shapes = IconButtonShapes(shape = CircleShape, pressedShape = MaterialTheme.shapes.small),
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.edit),
                            contentDescription = "Settings",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.weight(0.4f))
        }
    }

    // --- settings bottom sheet ---

    if (showSettings) {
        HexisBottomSheet(
            onDismissRequest = { showSettings = false },
            padding = 0.dp,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                item { SettingsField("Work", focusText, { focusText = it }, "min") }
                item { SettingsField("Short break", shortBreakText, { shortBreakText = it }, "min") }
                item { SettingsField("Long break", longBreakText, { longBreakText = it }, "min") }
                item { SettingsField("Long break every", intervalText, { intervalText = it }, "focuses") }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(
                            onClick = { applyPomodoroSettings() },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                            shapes = ButtonShapes(shape = MaterialTheme.shapes.extraLarge, pressedShape = MaterialTheme.shapes.small),
                            colors = ButtonDefaults.buttonColors(
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
        PomodoroAnalytics(onDismiss = { showAnalytics = false })
    }
}

@Composable
private fun SettingsField(label: String, value: String, onValueChange: (String) -> Unit, suffix: String) {
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
