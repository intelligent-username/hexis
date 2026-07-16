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

package com.loc.hexis.shared.ui.habit.ui.component.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loc.hexis.core.habits.PointsSummary
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import kotlin.math.maxOf

@Composable
fun PointsStatCards(
    pointsSummary: PointsSummary,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(
            label = "This Week",
            value = pointsSummary.currentWeekPoints,
            trend = computeTrend(pointsSummary),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Last Week",
            value = pointsSummary.lastWeekPoints,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "All Time",
            value = pointsSummary.totalPoints,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    trend: Pair<Float, Boolean>? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = flexFontEmphasis(),
                    fontWeight = FontWeight.Bold,
                ),
            )
            if (trend != null) {
                val (change, isUp) = trend
                val color = if (isUp) Color(0xFF4CAF50) else Color(0xFFE53935)
                val arrow = if (isUp) "\u25B2" else "\u25BC"
                Text(
                    text = "$arrow ${"%.0f".format(change)}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = flexFontRounded()),
                    color = color,
                )
            }
        }
    }
}

private fun computeTrend(summary: PointsSummary): Pair<Float, Boolean>? {
    if (summary.lastWeekPoints == 0 && summary.currentWeekPoints == 0) return null
    val change = ((summary.currentWeekPoints - summary.lastWeekPoints).toFloat() /
        maxOf(summary.lastWeekPoints, 1)) * 100f
    return change to (change >= 0)
}
