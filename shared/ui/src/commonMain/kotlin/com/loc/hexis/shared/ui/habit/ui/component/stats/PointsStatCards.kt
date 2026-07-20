package com.loc.hexis.shared.ui.habit.ui.component.stats

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loc.hexis.core.habits.PointsSummary
import com.loc.hexis.shared.ui.theme.flexFontEmphasis
import com.loc.hexis.shared.ui.theme.flexFontRounded
import kotlin.math.max

@Composable
fun PointsStatCards(pointsSummary: PointsSummary, modifier: Modifier = Modifier) {
    val previous = pointsSummary.lastWeekPoints
    val current = pointsSummary.currentWeekPoints

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard(label = "This Week So Far", value = current, modifier = Modifier.weight(1f))
        StatCard(label = "Same Time Last Week", value = previous, modifier = Modifier.weight(1f))
        StatChangeCard(previous = previous, current = current, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatChangeCard(previous: Int, current: Int, modifier: Modifier = Modifier) {
    val (displayText, displayColor) =
        if (previous == 0 && current == 0) {
            "—" to MaterialTheme.colorScheme.onSurface
        } else {
            val percent = ((current - previous).toFloat() / max(previous, 1)) * 100
            val intPercent = percent.toInt()
            when {
                intPercent > 0 -> "+${intPercent}%" to MaterialTheme.colorScheme.tertiary
                intPercent < 0 -> "${intPercent}%" to MaterialTheme.colorScheme.error
                else -> "0%" to MaterialTheme.colorScheme.onSurface
            }
        }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = displayText,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = flexFontEmphasis(),
                        fontWeight = FontWeight.Bold,
                    ),
                color = displayColor,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Change",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = flexFontRounded(),
                    fontSize = 9.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value.toString(),
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = flexFontEmphasis(),
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = flexFontRounded(),
                    fontSize = 9.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
