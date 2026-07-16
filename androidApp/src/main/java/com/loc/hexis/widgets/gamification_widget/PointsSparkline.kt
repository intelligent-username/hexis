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
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Arrangement
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.unit.dp

@GlanceComposable
fun PointsSparkline(
    weeklyPoints: List<Int>,
    modifier: GlanceModifier = GlanceModifier,
    maxBars: Int = 26,
) {
    val data = weeklyPoints.takeLast(maxBars)
    if (data.isEmpty()) return

    val maxVal = data.maxOrNull()?.coerceAtLeast(1) ?: 1
    val barWidth = 8.dp
    val spacing = 2.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(modifier.height.takeOrElse { 50.dp }),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = androidx.glance.layout.Alignment.Bottom,
    ) {
        data.forEach { points ->
            val heightPercent = (points.toFloat() / maxVal).coerceIn(0.04f, 1f)
            Box(
                modifier = GlanceModifier
                    .width(barWidth)
                    .fillMaxHeight(heightPercent)
                    .background(GlanceTheme.colors.primary, GlanceRoundedCornerShape(2.dp))
                    .align(androidx.glance.layout.Alignment.BottomCenter)
            )
        }
    }
}
