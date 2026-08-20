package com.loc.hexis.widgets.pomodoro_widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.loc.hexis.core.data.AppIntents
import com.loc.hexis.core.interfaces.ActivePomodoroSessionData
import com.loc.hexis.core.interfaces.SettingsDatastore
import com.loc.hexis.core.interfaces.ThemeDatastore
import com.loc.hexis.core.interfaces.WidgetActions
import com.loc.hexis.core.tasks.PomodoroRepo
import com.loc.hexis.core.tasks.PomodoroSettings
import com.loc.hexis.core.tasks.PomodoroStats
import com.loc.hexis.core.theme.AppTheme
import com.loc.hexis.core.theme.PaletteStyle
import com.loc.hexis.widgets.R
import com.loc.hexis.widgets.rememberWidgetColorProviders
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class PomodoroWidget : GlanceAppWidget(), KoinComponent {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settingsDatastore = get<SettingsDatastore>()
        val pomodoroRepo = get<PomodoroRepo>()
        val themeDatastore = get<ThemeDatastore>()

        provideContent {
            val size = LocalSize.current
            val pomodoroSettings by
                settingsDatastore.getPomodoroSettings().collectAsState(initial = PomodoroSettings())
            val activeSessionData by
                settingsDatastore.getActivePomodoroSessionData().collectAsState(initial = null)
            val todayStats by pomodoroRepo.getTodayStatsFlow().collectAsState(initial = null)

            val appTheme by
                themeDatastore.getAppThemeFlow().collectAsState(initial = AppTheme.SYSTEM)
            val seedColor by themeDatastore.getSeedColorFlow().collectAsState(initial = 0xFFFFFF)
            val isAmoled by themeDatastore.getAmoledPref().collectAsState(initial = false)
            val paletteStyle by
                themeDatastore.getPaletteStyle().collectAsState(initial = PaletteStyle.TONALSPOT)
            val isMaterialYou by themeDatastore.getMaterialYouFlow().collectAsState(initial = false)

            val colors =
                rememberWidgetColorProviders(
                    appTheme = appTheme,
                    seedColor = seedColor,
                    isAmoled = isAmoled,
                    paletteStyle = paletteStyle,
                    isMaterialYou = isMaterialYou,
                )

            key(size, pomodoroSettings, activeSessionData, todayStats) {
                GlanceTheme(colors = colors) {
                    PomodoroWidgetContent(
                        settings = pomodoroSettings,
                        activeSession = activeSessionData,
                        stats = todayStats,
                    )
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            val colors =
                rememberWidgetColorProviders(
                    appTheme = AppTheme.LIGHT,
                    seedColor = 0xFFFFFF,
                    isAmoled = false,
                    paletteStyle = PaletteStyle.TONALSPOT,
                    isMaterialYou = false,
                )
            GlanceTheme(colors = colors) {
                PomodoroWidgetContent(
                    settings = PomodoroSettings(focusMinutes = 25f),
                    activeSession = null,
                    stats = PomodoroStats(sessionCount = 4, totalMinutes = 100f),
                )
            }
        }
    }
}

private fun colorToInt(c: Color): Int =
    ((c.alpha * 255f).roundToInt() shl 24) or
        ((c.red * 255f).roundToInt() shl 16) or
        ((c.green * 255f).roundToInt() shl 8) or
        ((c.blue * 255f).roundToInt())

@GlanceComposable
@Composable
private fun PomodoroRing(
    progress: Float,
    primaryColor: Color,
    trackColor: Color,
    modifier: GlanceModifier = GlanceModifier,
    sizePx: Int = 140,
) {
    val primInt = colorToInt(primaryColor)
    val trkInt = colorToInt(trackColor)

    val bitmap =
        remember(progress, primInt, trkInt, sizePx) {
            val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val strokeW = sizePx * 0.12f
            val halfStroke = strokeW / 2f
            val rect = RectF(halfStroke, halfStroke, sizePx - halfStroke, sizePx - halfStroke)

            val paint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = strokeW
                    strokeCap = Paint.Cap.ROUND
                }

            // Draw background track
            paint.color = trkInt
            canvas.drawArc(rect, 0f, 360f, false, paint)

            // Draw progress arc
            if (progress > 0.005f) {
                paint.color = primInt
                val sweep = (progress.coerceIn(0f, 1f) * 360f)
                canvas.drawArc(rect, -90f, sweep, false, paint)
            }

            bmp
        }

    Image(
        provider = ImageProvider(bitmap),
        contentDescription = "Pomodoro Progress Ring",
        modifier = modifier,
    )
}

@GlanceComposable
@Composable
private fun PomodoroWidgetContent(
    settings: PomodoroSettings,
    activeSession: ActivePomodoroSessionData?,
    stats: PomodoroStats?,
) {
    val context = LocalContext.current
    val size = LocalSize.current

    val openRootAction =
        actionStartActivity(AppIntents.openMain(context, WidgetActions.OPEN_POMODORO))
    val startFocusAction =
        actionStartActivity(AppIntents.openMain(context, WidgetActions.OPEN_POMODORO_START))
    val openSettingsAction =
        actionStartActivity(AppIntents.openMain(context, WidgetActions.OPEN_POMODORO_SETTINGS))
    val openHistoryAction =
        actionStartActivity(AppIntents.openMain(context, WidgetActions.OPEN_POMODORO_HISTORY))

    val nowMs = Clock.System.now().toEpochMilliseconds()
    val isSessionRunning =
        activeSession != null && !activeSession.isPaused && activeSession.targetEndTimeMillis > nowMs
    val isSessionPaused = activeSession != null && activeSession.isPaused

    val (timeDisplay, statusLabel, progressFraction) =
        when {
            isSessionRunning && activeSession != null -> {
                val totalSec =
                    if (activeSession.totalDurationSeconds > 0) activeSession.totalDurationSeconds
                    else max((activeSession.focusMinutes * 60).toInt(), 1)
                val remainingSec =
                    ((activeSession.targetEndTimeMillis - nowMs) / 1000).coerceAtLeast(0).toInt()
                val m = remainingSec / 60
                val s = remainingSec % 60
                val fraction = (totalSec - remainingSec).toFloat() / totalSec.toFloat()
                val formatted = "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
                Triple(formatted, "Focusing", fraction.coerceIn(0f, 1f))
            }
            isSessionPaused && activeSession != null -> {
                val totalSec =
                    if (activeSession.totalDurationSeconds > 0) activeSession.totalDurationSeconds
                    else max((activeSession.focusMinutes * 60).toInt(), 1)
                val remainingSec = activeSession.pausedSecondsRemaining.coerceAtLeast(0)
                val m = remainingSec / 60
                val s = remainingSec % 60
                val fraction = (totalSec - remainingSec).toFloat() / totalSec.toFloat()
                val formatted = "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
                Triple(formatted, "Paused", fraction.coerceIn(0f, 1f))
            }
            else -> {
                val focusM = settings.focusMinutes.toInt()
                Triple("${focusM.toString().padStart(2, '0')}:00", "Ready to Focus", 0f)
            }
        }

    val isSessionActive = isSessionRunning || isSessionPaused
    val completedCount = stats?.sessionCount ?: 0
    val totalMinutes = stats?.totalMinutes?.toInt() ?: 0

    val primaryResolved = GlanceTheme.colors.primary.getColor(context)
    val surfaceVariantResolved = GlanceTheme.colors.surfaceVariant.getColor(context)

    // Graduated degradation resizing hierarchy:
    // 1) Full Mode (Buttons + Circle + Time + Status): width >= 195.dp && height >= 85.dp
    // 2) Medium Mode (Circle + Time + Status, no buttons): width >= 110.dp && height >= 65.dp
    // 3) Minimal Icon Mode (Pure Circle): tiny sizes (1x1 or very small cells)
    val showButtons = size.width >= 195.dp && size.height >= 85.dp
    val showTimeText = size.width >= 110.dp && size.height >= 65.dp

    // Dynamic typography scaling as widget gets smaller
    val timerFontSize: TextUnit =
        when {
            size.width >= 250.dp && size.height >= 120.dp -> 28.sp
            size.width >= 180.dp && size.height >= 85.dp -> 22.sp
            size.width >= 140.dp -> 18.sp
            else -> 15.sp
        }

    val statusFontSize: TextUnit =
        when {
            size.width >= 220.dp -> 12.sp
            size.width >= 160.dp -> 11.sp
            else -> 10.sp
        }

    // Dynamic ring sizing (gets smaller as widget resizes)
    val ringSize: Dp =
        when {
            !showTimeText -> (minOf(size.width.value, size.height.value) * 0.72f).dp.coerceIn(36.dp, 80.dp)
            size.width >= 250.dp && size.height >= 120.dp -> 58.dp
            size.width >= 180.dp && size.height >= 85.dp -> 46.dp
            size.height >= 90.dp -> 42.dp
            else -> 36.dp
        }

    val ringIconSize: Dp =
        when {
            ringSize >= 56.dp -> 20.dp
            ringSize >= 44.dp -> 16.dp
            else -> 13.dp
        }

    // Proportional button sizing that scales with height so it never collides with top-right pill
    val buttonHeight: Dp =
        when {
            size.height >= 130.dp -> 27.dp
            size.height >= 105.dp -> 23.dp
            else -> 19.dp
        }

    val buttonTextSize: TextUnit =
        when {
            size.height >= 130.dp -> 11.sp
            size.height >= 105.dp -> 10.sp
            else -> 9.sp
        }

    val buttonIconSize: Dp =
        when {
            size.height >= 130.dp -> 13.dp
            size.height >= 105.dp -> 11.dp
            else -> 9.dp
        }

    val buttonTopGap: Dp =
        when {
            size.height >= 130.dp -> 20.dp
            size.height >= 105.dp -> 16.dp
            else -> 12.dp
        }

    val buttonSpacing: Dp =
        when {
            size.height >= 130.dp -> 4.dp
            size.height >= 105.dp -> 3.dp
            else -> 2.dp
        }

    val buttonColumnWidth: Dp =
        when {
            size.width >= 240.dp -> 106.dp
            size.width >= 210.dp -> 96.dp
            else -> 88.dp
        }

    Box(
        modifier =
            GlanceModifier.fillMaxSize()
                .cornerRadius(20.dp)
                .background(GlanceTheme.colors.widgetBackground)
                .clickable(openRootAction)
                .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        // Tag always anchored in the top-right corner
        if (size.width >= 130.dp && size.height >= 55.dp) {
            Box(
                modifier = GlanceModifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd,
            ) {
                Box(
                    modifier =
                        GlanceModifier.cornerRadius(8.dp)
                            .background(GlanceTheme.colors.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$completedCount done · ${totalMinutes}m",
                        style =
                            TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (size.height < 90.dp) 9.sp else 10.sp,
                                color = GlanceTheme.colors.onSurfaceVariant,
                            ),
                        maxLines = 1,
                    )
                }
            }
        }

        // Main Widget Body
        if (showButtons) {
            // Full Mode: Left = Countdown Ring & Scaled Timer, Right = 3 Vertically Stacked Scaled Buttons below tag
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left Area: Ring + Time & Status
                Row(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = GlanceModifier.size(ringSize),
                        contentAlignment = Alignment.Center,
                    ) {
                        PomodoroRing(
                            progress = if (isSessionActive) progressFraction else 1f,
                            primaryColor = primaryResolved,
                            trackColor = surfaceVariantResolved,
                            modifier = GlanceModifier.fillMaxSize(),
                            sizePx = 160,
                        )
                        Image(
                            provider =
                                ImageProvider(
                                    if (isSessionRunning) R.drawable.ic_pomodoro
                                    else R.drawable.ic_play
                                ),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                            modifier = GlanceModifier.size(ringIconSize),
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = statusLabel,
                            style =
                                TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = statusFontSize,
                                    color =
                                        if (isSessionRunning) GlanceTheme.colors.primary
                                        else GlanceTheme.colors.onSurfaceVariant,
                                ),
                            maxLines = 1,
                        )
                        Spacer(modifier = GlanceModifier.height(1.dp))
                        Text(
                            text = timeDisplay,
                            style =
                                TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = timerFontSize,
                                    color = GlanceTheme.colors.onSurface,
                                ),
                            maxLines = 1,
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.width(6.dp))

                // Right Area: 3 Vertically Stacked Buttons positioned cleanly below the stats pill
                Column(
                    modifier = GlanceModifier.width(buttonColumnWidth),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = GlanceModifier.height(buttonTopGap))

                    // Button 1: Start Focus / Resume
                    Box(
                        modifier =
                            GlanceModifier.fillMaxWidth()
                                .height(buttonHeight)
                                .cornerRadius(6.dp)
                                .background(
                                    if (isSessionRunning) GlanceTheme.colors.secondaryContainer
                                    else GlanceTheme.colors.primary
                                )
                                .clickable(startFocusAction)
                                .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                provider =
                                    ImageProvider(
                                        if (isSessionRunning) R.drawable.ic_pomodoro
                                        else R.drawable.ic_play
                                    ),
                                contentDescription = null,
                                colorFilter =
                                    ColorFilter.tint(
                                        if (isSessionRunning)
                                            GlanceTheme.colors.onSecondaryContainer
                                        else GlanceTheme.colors.onPrimary
                                    ),
                                modifier = GlanceModifier.size(buttonIconSize),
                            )
                            Spacer(modifier = GlanceModifier.width(3.dp))
                            Text(
                                text = if (isSessionRunning) "Resume" else "Start Focus",
                                style =
                                    TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = buttonTextSize,
                                        color =
                                            if (isSessionRunning)
                                                GlanceTheme.colors.onSecondaryContainer
                                            else GlanceTheme.colors.onPrimary,
                                    ),
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(buttonSpacing))

                    // Button 2: Edit Sessions
                    Box(
                        modifier =
                            GlanceModifier.fillMaxWidth()
                                .height(buttonHeight)
                                .cornerRadius(6.dp)
                                .background(GlanceTheme.colors.surfaceVariant)
                                .clickable(openSettingsAction)
                                .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_settings),
                                contentDescription = null,
                                colorFilter =
                                    ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                                modifier = GlanceModifier.size(buttonIconSize),
                            )
                            Spacer(modifier = GlanceModifier.width(3.dp))
                            Text(
                                text = "Edit",
                                style =
                                    TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = buttonTextSize,
                                        color = GlanceTheme.colors.onSurfaceVariant,
                                    ),
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(buttonSpacing))

                    // Button 3: History
                    Box(
                        modifier =
                            GlanceModifier.fillMaxWidth()
                                .height(buttonHeight)
                                .cornerRadius(6.dp)
                                .background(GlanceTheme.colors.surfaceVariant)
                                .clickable(openHistoryAction)
                                .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_history),
                                contentDescription = null,
                                colorFilter =
                                    ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                                modifier = GlanceModifier.size(buttonIconSize),
                            )
                            Spacer(modifier = GlanceModifier.width(3.dp))
                            Text(
                                text = "History",
                                style =
                                    TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = buttonTextSize,
                                        color = GlanceTheme.colors.onSurfaceVariant,
                                    ),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        } else if (showTimeText) {
            // Medium Mode: Buttons disappeared, Countdown circle + scaled time display
            if (size.width >= 150.dp) {
                // Wide horizontal row (2x1, 3x1)
                Row(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = GlanceModifier.size(ringSize).clickable(startFocusAction),
                        contentAlignment = Alignment.Center,
                    ) {
                        PomodoroRing(
                            progress = if (isSessionActive) progressFraction else 1f,
                            primaryColor = primaryResolved,
                            trackColor = surfaceVariantResolved,
                            modifier = GlanceModifier.fillMaxSize(),
                            sizePx = 140,
                        )
                        Image(
                            provider =
                                ImageProvider(
                                    if (isSessionRunning) R.drawable.ic_pomodoro
                                    else R.drawable.ic_play
                                ),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                            modifier = GlanceModifier.size(ringIconSize),
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = statusLabel,
                            style =
                                TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = statusFontSize,
                                    color =
                                        if (isSessionRunning) GlanceTheme.colors.primary
                                        else GlanceTheme.colors.onSurfaceVariant,
                                ),
                            maxLines = 1,
                        )
                        Text(
                            text = timeDisplay,
                            style =
                                TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = timerFontSize,
                                    color = GlanceTheme.colors.onSurface,
                                ),
                            maxLines = 1,
                        )
                    }
                }
            } else {
                // Vertical column (2x2)
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Box(
                        modifier = GlanceModifier.size(ringSize).clickable(startFocusAction),
                        contentAlignment = Alignment.Center,
                    ) {
                        PomodoroRing(
                            progress = if (isSessionActive) progressFraction else 1f,
                            primaryColor = primaryResolved,
                            trackColor = surfaceVariantResolved,
                            modifier = GlanceModifier.fillMaxSize(),
                            sizePx = 140,
                        )
                        Image(
                            provider =
                                ImageProvider(
                                    if (isSessionRunning) R.drawable.ic_pomodoro
                                    else R.drawable.ic_play
                                ),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                            modifier = GlanceModifier.size(ringIconSize),
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(2.dp))

                    Text(
                        text = timeDisplay,
                        style =
                            TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = timerFontSize,
                                color = GlanceTheme.colors.onSurface,
                            ),
                        maxLines = 1,
                    )

                    if (size.height >= 100.dp) {
                        Spacer(modifier = GlanceModifier.height(1.dp))
                        Text(
                            text = statusLabel,
                            style =
                                TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = statusFontSize,
                                    color =
                                        if (isSessionRunning) GlanceTheme.colors.primary
                                        else GlanceTheme.colors.onSurfaceVariant,
                                ),
                            maxLines = 1,
                        )
                    }
                }
            }
        } else {
            // Minimal Mode: Text disappears, pure countdown circle with icon
            Box(
                modifier = GlanceModifier.fillMaxSize().clickable(startFocusAction),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = GlanceModifier.size(ringSize),
                    contentAlignment = Alignment.Center,
                ) {
                    PomodoroRing(
                        progress = if (isSessionActive) progressFraction else 1f,
                        primaryColor = primaryResolved,
                        trackColor = surfaceVariantResolved,
                        modifier = GlanceModifier.fillMaxSize(),
                        sizePx = 140,
                    )
                    Image(
                        provider =
                            ImageProvider(
                                if (isSessionRunning) R.drawable.ic_pomodoro
                                else R.drawable.ic_play
                            ),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                        modifier = GlanceModifier.size(ringIconSize),
                    )
                }
            }
        }
    }
}
