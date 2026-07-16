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

package com.loc.hexis.widgets.gamification_widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.ActionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Arrangement
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.fontWeight
import androidx.glance.unit.dp
import androidx.glance.unit.sp
import com.loc.hexis.core.habits.OverallAnalytics
import com.loc.hexis.core.habits.PointsTrend
import com.loc.hexis.core.now
import com.loc.hexis.shared.ui.theme.flexFontRounded
import kotlinx.datetime.LocalDateTime

@GlanceComposable
fun GamificationWidgetContent(
    analytics: OverallAnalytics,
    trend: PointsTrend,
    onRefresh: () -> Unit,
    onOpenApp: ActionStartActivity,
    modifier: GlanceModifier = GlanceModifier,
) {
    val size = LocalSize.current
    val isCompact = size.width < 200.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(onOpenApp)
    ) {
        if (isCompact) CompactLayout(trend, analytics)
        else if (size.height < 150.dp) WideLayout(trend, analytics, onRefresh)
        else LargeLayout(trend, analytics, onRefresh)
    }
}

@GlanceComposable
fun CompactLayout(trend: PointsTrend, analytics: OverallAnalytics) {
    Column(modifier = GlanceModifier.fillMaxSize().padding(12.dp)) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = trend.currentWeekPoints.toString(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface,
            )
            Box(
                modifier = GlanceModifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(
                        color = if (trend.netChange >= 0) Color.Green else Color.Red,
                        shape = GlanceRoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${if (trend.netChange > 0) "+" else ""}${trend.netChange} (${"%.0f".format(trend.netChangePercent)}%)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        PointsSparkline(
            weeklyPoints = trend.weeklyPoints.map { it.pointsEarned }.takeLast(8),
            modifier = GlanceModifier.fillMaxWidth().height(40.dp),
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${"%.0f".format(analytics.consistency * 100)}% completion",
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurfaceVariant,
            )
            Text(
                text = "Tap for Analytics",
                fontSize = 10.sp,
                color = GlanceTheme.colors.primary,
            )
        }
    }
}

@GlanceComposable
fun WideLayout(trend: PointsTrend, analytics: OverallAnalytics, onRefresh: () -> Unit) {
    Column(modifier = GlanceModifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Gamification",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Box(
                modifier = GlanceModifier
                    .width(32.dp)
                    .height(32.dp)
                    .background(GlanceTheme.colors.surfaceContainerHigh, CircleShape)
                    .clickable { onRefresh() }
            ) {
                Image(
                    provider = ImageProvider(R.drawable.refresh),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier.width(18.dp).height(18.dp).padding(7.dp),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatBox("This Week", trend.currentWeekPoints.toString(), "pts", GlanceTheme.colors.primary)
            StatBox("Last Week", trend.previousWeekPoints.toString(), "pts", GlanceTheme.colors.onSurfaceVariant)
            StatBox("Net Change", "${if (trend.netChange > 0) "+" else ""}${trend.netChange}", "${"%.0f".format(trend.netChangePercent)}%",
                if (trend.netChange >= 0) Color.Green else Color.Red)
            StatBox("Rate", "${"%.0f".format(analytics.consistency * 100)}%", "", GlanceTheme.colors.secondary)
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        PointsSparkline(
            weeklyPoints = trend.weeklyPoints.map { it.pointsEarned }.takeLast(16),
            modifier = GlanceModifier.fillMaxWidth().height(50.dp),
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Text(
            text = "Total: ${trend.totalPointsAllTime}  •  Avg: ${"%.0f".format(trend.averageWeeklyPoints)}/wk  •  Best: ${trend.bestWeekPoints}  •  Streak: ${trend.currentStreakWeeks}w",
            fontSize = 10.sp,
            color = GlanceTheme.colors.onSurfaceVariant,
        )
    }
}

@GlanceComposable
fun LargeLayout(trend: PointsTrend, analytics: OverallAnalytics, onRefresh: () -> Unit) {
    Column(modifier = GlanceModifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "POINTS TREND",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = GlanceTheme.colors.onSurfaceVariant,
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LargeStatCard("This Week", trend.currentWeekPoints.toString(), "pts", GlanceTheme.colors.primary)
            LargeStatCard("Last Week", trend.previousWeekPoints.toString(), "pts", GlanceTheme.colors.onSurfaceVariant)
            LargeStatCard("Net Change", "${if (trend.netChange > 0) "+" else ""}${trend.netChange}", "${"%.0f".format(trend.netChangePercent)}%",
                if (trend.netChange >= 0) Color.Green else Color.Red)
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        PointsSparkline(
            weeklyPoints = trend.weeklyPoints.map { it.pointsEarned },
            modifier = GlanceModifier.fillMaxWidth().height(80.dp),
        )

        Spacer(modifier = GlanceModifier.height(12.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricBox("Rate", "${"%.0f".format(analytics.consistency * 100)}%")
            MetricBox("Done", "${analytics.completedHabits}/${analytics.completedHabits + analytics.missedHabits}")
            MetricBox("Total", trend.totalPointsAllTime.toString())
            MetricBox("Streak", "${trend.currentStreakWeeks}w")
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tap to open Analytics",
                fontSize = 10.sp,
                color = GlanceTheme.colors.primary,
            )
            Box(
                modifier = GlanceModifier
                    .width(32.dp)
                    .height(32.dp)
                    .background(GlanceTheme.colors.surfaceContainerHigh, CircleShape)
                    .clickable { onRefresh() }
            ) {
                Image(
                    provider = ImageProvider(R.drawable.refresh),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier.width(18.dp).height(18.dp).padding(7.dp),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                )
            }
        }
    }
}

@GlanceComposable
fun StatBox(label: String, value: String, unit: String, valueColor: Color) {
    Column(
        modifier = GlanceModifier
            .defaultWeight()
            .padding(12.dp)
            .background(GlanceTheme.colors.surfaceContainerHigh, GlanceRoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = label, fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant)
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
        if (unit.isNotBlank()) {
            Text(text = unit, fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant)
        }
    }
}

@GlanceComposable
fun LargeStatCard(label: String, value: String, unit: String, valueColor: Color) {
    Column(
        modifier = GlanceModifier
            .defaultWeight()
            .padding(16.dp)
            .background(GlanceTheme.colors.surfaceContainerHigh, GlanceRoundedCornerShape(16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = label, fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant)
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(text = unit, fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant)
    }
}

@GlanceComposable
fun MetricBox(label: String, value: String) {
    Column(
        modifier = GlanceModifier
            .defaultWeight()
            .padding(12.dp)
            .background(GlanceTheme.colors.surfaceContainerHigh, GlanceRoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = label, fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant)
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
