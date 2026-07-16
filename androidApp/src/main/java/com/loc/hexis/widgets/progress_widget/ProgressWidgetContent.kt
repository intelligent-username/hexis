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
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface,
                        ),
                )
                Text(
                    text = "this week so far",
                    style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant),
                )
            }
            val netUp = trend.partialNetChange >= 0
            Box(
                modifier =
                    GlanceModifier.background(
                            if (netUp) GlanceTheme.colors.tertiary else GlanceTheme.colors.error
                        )
                        .cornerRadius(8.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text =
                        "${if (trend.partialNetChange > 0) "+" else ""}${trend.partialNetChange}",
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                if (netUp) GlanceTheme.colors.onTertiary
                                else GlanceTheme.colors.onError,
                        ),
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        val last14Days = dailyData(analytics.heatMapData)
        ProgressLineGraph(
            dataPoints = last14Days,
            modifier = GlanceModifier.fillMaxWidth().height(46.dp),
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier =
                    GlanceModifier.background(GlanceTheme.colors.secondaryContainer)
                        .cornerRadius(12.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${"%.0f".format(analytics.consistency * 100)}%",
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSecondaryContainer,
                        ),
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = "consistency",
                    style =
                        TextStyle(fontSize = 9.sp, color = GlanceTheme.colors.onSecondaryContainer),
                )
            }
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "Tap for Analytics",
                style = TextStyle(fontSize = 9.sp, color = GlanceTheme.colors.primary),
            )
        }
    }
}

@Composable
@GlanceComposable
private fun FullContent(trend: PointsTrend, analytics: OverallAnalytics) {
    Column(modifier = GlanceModifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 6.dp)) {
        // Primary: StatPill row — larger fonts, prominent comparison
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            StatPill(
                label = "This Week",
                value = trend.currentPartialPoints.toString(),
                icon = R.drawable.check_circle,
                tint = GlanceTheme.colors.primary,
                modifier = GlanceModifier.defaultWeight(),
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            StatPill(
                label = "Last Week",
                value = trend.previousPartialPoints.toString(),
                icon = R.drawable.circle_border,
                tint = GlanceTheme.colors.onSurfaceVariant,
                modifier = GlanceModifier.defaultWeight(),
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            val netUp = trend.partialNetChange >= 0
            StatPill(
                label = "Change",
                value = "${if (trend.partialNetChange > 0) "+" else ""}${trend.partialNetChange}",
                icon = R.drawable.arrow_forward,
                tint = if (netUp) GlanceTheme.colors.tertiary else GlanceTheme.colors.error,
                modifier = GlanceModifier.defaultWeight(),
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Secondary row: MetricPills + mini graph side by side
        val last14Days = dailyData(analytics.heatMapData)
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                MetricPill(
                    label = "Consistency",
                    value = "${"%.0f".format(analytics.consistency * 100)}%",
                    icon = R.drawable.heat,
                    modifier = GlanceModifier.fillMaxWidth(),
                )
                Spacer(modifier = GlanceModifier.height(3.dp))
                MetricPill(
                    label = "Streak",
                    value = "${trend.currentStreakWeeks}w",
                    icon = R.drawable.heat_outlined,
                    modifier = GlanceModifier.fillMaxWidth(),
                )
                Spacer(modifier = GlanceModifier.height(3.dp))
                MetricPill(
                    label = "Total",
                    value = trend.totalPointsAllTime.toString(),
                    icon = R.drawable.analytics,
                    modifier = GlanceModifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = GlanceModifier.width(6.dp))
            ProgressLineGraph(
                dataPoints = last14Days,
                modifier = GlanceModifier.defaultWeight().height(80.dp),
            )
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        Text(
            text = "Tap to open Analytics \u2192",
            style = TextStyle(fontSize = 8.sp, color = GlanceTheme.colors.primary),
        )
    }
}

@Composable
@GlanceComposable
private fun StatPill(
    label: String,
    value: String,
    icon: Int,
    tint: androidx.glance.unit.ColorProvider,
    modifier: GlanceModifier = GlanceModifier,
) {
    Column(
        modifier =
            modifier
                .padding(3.dp)
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = GlanceModifier.height(6.dp))
        Image(
            provider = ImageProvider(icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(tint),
            modifier = GlanceModifier.width(18.dp).height(18.dp),
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = value,
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = tint),
        )
        Text(
            text = label,
            style = TextStyle(fontSize = 9.sp, color = GlanceTheme.colors.onSurfaceVariant),
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
    }
}

@Composable
@GlanceComposable
private fun MetricPill(
    label: String,
    value: String,
    icon: Int,
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        modifier =
            modifier
                .padding(2.dp)
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(10.dp)
                .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
            modifier = GlanceModifier.width(12.dp).height(12.dp),
        )
        Spacer(modifier = GlanceModifier.width(3.dp))
        Column {
            Text(
                text = value,
                style =
                    TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface,
                    ),
            )
            Text(
                text = label,
                style = TextStyle(fontSize = 7.sp, color = GlanceTheme.colors.onSurfaceVariant),
            )
        }
    }
}

private fun dailyData(heatMapData: Map<LocalDate, Int>, days: Int = 14): List<Int> {
    val today = LocalDate.now()
    return (days - 1 downTo 0).map { offset ->
        heatMapData[today.minus(offset, DateTimeUnit.DAY)] ?: 0
    }
}
