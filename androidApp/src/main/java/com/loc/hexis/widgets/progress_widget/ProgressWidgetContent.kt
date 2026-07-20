package com.loc.hexis.widgets.progress_widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.loc.hexis.R
import com.loc.hexis.core.habits.OverallAnalytics
import com.loc.hexis.core.habits.PointsTrend
import com.loc.hexis.core.now
import com.loc.hexis.widgets.WidgetSize
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

@Composable
@GlanceComposable
fun ProgressWidgetContent(
    analytics: OverallAnalytics,
    trend: PointsTrend,
    onRefresh: () -> Unit,
    onOpenApp: Action,
    modifier: GlanceModifier = GlanceModifier,
) {
    val size = LocalSize.current
    val roundedCornerSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .then(
                    if (roundedCornerSupported) {
                        GlanceModifier.background(GlanceTheme.colors.widgetBackground)
                            .cornerRadius(24.dp)
                    } else {
                        GlanceModifier.background(
                            imageProvider = ImageProvider(R.drawable.rounded_4dp),
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.widgetBackground),
                        )
                    }
                )
                .clickable(onOpenApp)
    ) {
        if (size.height >= 100.dp) {
            WidgetTitleBar(size.width >= WidgetSize.Width4, onRefresh)
        }

        Box(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            val graphHeightDp = if (size.height < 100.dp) size.height else size.height - 48.dp
            ProgressLineGraph(
                dailyData = dailyData(analytics.heatMapData),
                weeklyData = trend.weeklyPoints.map { it.pointsEarned },
                bestWeek = trend.bestWeekPoints,
                currentPartial = trend.currentPartialPoints,
                modifier = GlanceModifier.fillMaxSize(),
                graphHeight = graphHeightDp,
            )

            if (size.height >= 140.dp) {
                PtsHeader(trend, compact = size.height < 200.dp)
            }

            if (size.height >= 200.dp) {
                Box(
                    modifier = GlanceModifier.fillMaxSize().padding(start = 10.dp, bottom = 4.dp),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    StatsFooter(analytics)
                }
            }
        }
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
@GlanceComposable
private fun WidgetTitleBar(showRefresh: Boolean, onRefresh: () -> Unit) {
    TitleBar(
        startIcon = ImageProvider(R.drawable.analytics),
        title = "Progress",
        actions = {
            if (showRefresh) {
                Box(GlanceModifier.padding(horizontal = 16.dp)) {
                    Image(
                        provider = ImageProvider(R.drawable.refresh),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
                        modifier = GlanceModifier.clickable { onRefresh() },
                    )
                }
            } else {
                Spacer(modifier = GlanceModifier.width(16.dp))
            }
        },
    )
}

/** Large primary number with delta. Compact: one line. Full: slightly larger. */
@Composable
@GlanceComposable
private fun PtsHeader(trend: PointsTrend, compact: Boolean) {
    val netUp = trend.partialNetChange >= 0
    val deltaColor = if (netUp) GlanceTheme.colors.tertiary else GlanceTheme.colors.error
    val deltaText = "${if (netUp) "+" else ""}${trend.partialNetChange}"
    val pctText = "${abs(trend.partialNetChangePercent).roundToInt()}%"
    val ptsFontSize = if (compact) 26.sp else 32.sp

    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Primary number
        Text(
            text = trend.currentPartialPoints.toString(),
            style =
                TextStyle(
                    fontSize = ptsFontSize,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface,
                ),
        )
        Spacer(GlanceModifier.width(4.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "pts",
                style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant),
            )
            Text(
                text = "this wk",
                style = TextStyle(fontSize = 8.sp, color = GlanceTheme.colors.onSurfaceVariant),
            )
        }
        Spacer(GlanceModifier.width(10.dp))
        // delta
        Text(
            text = "$deltaText ($pctText ${if (netUp) "↑" else "↓"})",
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = deltaColor),
        )

        // WoW Avg and Best stats (consistent partial-week numbers)
        Spacer(GlanceModifier.width(12.dp))
        Text(
            text = "avg ${trend.averageWeeklyPoints.roundToInt()}",
            style =
                TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurfaceVariant,
                ),
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
            text = "best ${trend.bestWeekPoints}",
            style =
                TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurfaceVariant,
                ),
        )

        Spacer(GlanceModifier.defaultWeight())
        // streak
        if (trend.currentStreakWeeks > 0) {
            Text(
                text = "streak: ${trend.currentStreakWeeks}w",
                style =
                    TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurfaceVariant,
                    ),
            )
        }
    }
}

/**
 * One-line footer strip — dense, no cards, no repeated info. Shows consistency blocks at the bottom
 * left.
 */
@Composable
@GlanceComposable
private fun StatsFooter(analytics: OverallAnalytics) {
    val consistencyPct = (analytics.consistency * 100).roundToInt()
    val filledBlocks = (analytics.consistency * 5).roundToInt().coerceIn(0, 5)
    val emptyBlocks = 5 - filledBlocks

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text =
                "■".repeat(filledBlocks) +
                    "□".repeat(emptyBlocks) +
                    " $consistencyPct% consistency",
            style =
                TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary,
                ),
        )
    }
}

private fun dailyData(heatMapData: Map<LocalDate, Int>, days: Int = 14): List<Int> {
    val today = LocalDate.now()
    return (days - 1 downTo 0).map { offset ->
        heatMapData[today.minus(offset, DateTimeUnit.DAY)] ?: 0
    }
}
