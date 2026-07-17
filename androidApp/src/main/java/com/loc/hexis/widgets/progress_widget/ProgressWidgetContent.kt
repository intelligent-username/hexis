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
        TitleBar(
            startIcon = ImageProvider(R.drawable.analytics),
            title = "Progress",
            actions = {
                if (size.width >= WidgetSize.Width4) {
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

        if (size.height < 140.dp) {
            CompactContent(trend, analytics)
        } else {
            FullContent(trend, analytics)
        }
    }
}

@Composable
@GlanceComposable
private fun CompactContent(trend: PointsTrend, analytics: OverallAnalytics) {
    Column(modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = trend.currentPartialPoints.toString(),
                    style =
                        TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface,
                        ),
                )
                Text(
                    text = "pts this week",
                    style = TextStyle(fontSize = 9.sp, color = GlanceTheme.colors.onSurfaceVariant),
                )
            }
            val netUp = trend.partialNetChange >= 0
            val changeColor = if (netUp) GlanceTheme.colors.tertiary else GlanceTheme.colors.error
            Box(
                modifier =
                    GlanceModifier.background(GlanceTheme.colors.secondaryContainer)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${if (netUp) "+" else ""}${trend.partialNetChange}",
                    style =
                        TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = changeColor,
                        ),
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        val last14Days = dailyData(analytics.heatMapData)
        ProgressLineGraph(
            dataPoints = last14Days,
            modifier = GlanceModifier.fillMaxWidth().height(48.dp),
        )

        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${"%.0f".format(analytics.consistency * 100)}%",
                    style =
                        TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.primary,
                        ),
                )
                Text(
                    text = " consistency",
                    style = TextStyle(fontSize = 8.sp, color = GlanceTheme.colors.onSurfaceVariant),
                )
            }
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "Analytics \u2192",
                style = TextStyle(fontSize = 8.sp, color = GlanceTheme.colors.primary),
            )
        }
    }
}

@Composable
@GlanceComposable
private fun FullContent(trend: PointsTrend, analytics: OverallAnalytics) {
    Column(modifier = GlanceModifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 4.dp)) {
        // Mini stat chips: ultra-compact inline row
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatChip(
                label = "This Week",
                value = trend.currentPartialPoints.toString(),
                tint = GlanceTheme.colors.primary,
                modifier = GlanceModifier.defaultWeight(),
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            StatChip(
                label = "Last Week",
                value = trend.previousPartialPoints.toString(),
                tint = GlanceTheme.colors.onSurfaceVariant,
                modifier = GlanceModifier.defaultWeight(),
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            val netUp = trend.partialNetChange >= 0
            val changeColor = if (netUp) GlanceTheme.colors.tertiary else GlanceTheme.colors.error
            StatChip(
                label = if (netUp) "+${trend.partialNetChange}" else trend.partialNetChange.toString(),
                value = "${"%.0f".format(kotlin.math.abs(trend.partialNetChangePercent))}%",
                tint = changeColor,
                modifier = GlanceModifier.defaultWeight(),
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Large graph
        val last14Days = dailyData(analytics.heatMapData)
        ProgressLineGraph(
            dataPoints = last14Days,
            modifier = GlanceModifier.fillMaxWidth().height(74.dp),
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Secondary meta row
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetaTag(text = "${"%.0f".format(analytics.consistency * 100)}% consistent")
            Spacer(modifier = GlanceModifier.width(8.dp))
            MetaTag(text = "${trend.currentStreakWeeks}w streak")
            Spacer(modifier = GlanceModifier.width(8.dp))
            MetaTag(text = "${trend.totalPointsAllTime} total")
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "\u2192",
                style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.primary),
            )
        }
    }
}

@Composable
@GlanceComposable
private fun StatChip(
    label: String,
    value: String,
    tint: androidx.glance.unit.ColorProvider,
    modifier: GlanceModifier = GlanceModifier,
) {
    Column(
        modifier =
            modifier
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(10.dp)
                .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style =
                TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = tint),
        )
        Text(
            text = label,
            style = TextStyle(fontSize = 8.sp, color = GlanceTheme.colors.onSurfaceVariant),
        )
    }
}

@Composable
@GlanceComposable
private fun MetaTag(text: String) {
    Text(
        text = text,
        style =
            TextStyle(
                fontSize = 8.sp,
                color = GlanceTheme.colors.onSurfaceVariant,
            ),
    )
}

private fun dailyData(heatMapData: Map<LocalDate, Int>, days: Int = 14): List<Int> {
    val today = LocalDate.now()
    return (days - 1 downTo 0).map { offset ->
        heatMapData[today.minus(offset, DateTimeUnit.DAY)] ?: 0
    }
}
