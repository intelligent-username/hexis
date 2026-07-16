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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.unit.ColorProvider
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun ProgressLineGraph(
    dataPoints: List<Int>,
    modifier: GlanceModifier = GlanceModifier,
    graphHeight: Dp = 80.dp,
    lineColor: ColorProvider = GlanceTheme.colors.primary,
) {
    val data = dataPoints.takeLast(14)
    if (data.size < 2) return

    val context = LocalContext.current
    val resolvedColor = lineColor.getColor(context)

    val colorInt =
        ((resolvedColor.alpha * 255f).roundToInt() shl 24) or
            ((resolvedColor.red * 255f).roundToInt() shl 16) or
            ((resolvedColor.green * 255f).roundToInt() shl 8) or
            ((resolvedColor.blue * 255f).roundToInt())

    val fillColorInt =
        (((resolvedColor.alpha * 0.12f * 255f).roundToInt()) shl 24) or
            ((resolvedColor.red * 255f).roundToInt() shl 16) or
            ((resolvedColor.green * 255f).roundToInt() shl 8) or
            ((resolvedColor.blue * 255f).roundToInt())

    val bitmap =
        remember(data, colorInt) {
            val width = 800
            val height = 240
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val paint = Paint().apply { isAntiAlias = true }

            val maxVal = max(data.maxOrNull() ?: 1, 1)
            val pad = 20f
            val gW = width - 2f * pad
            val gH = height - 2f * pad

            val pts =
                data.mapIndexed { i, v ->
                    val x = pad + (i.toFloat() / (data.size - 1).coerceAtLeast(1)) * gW
                    val y = height - pad - (v.toFloat() / maxVal) * gH
                    Offset(x, y)
                }

            // --- smooth cubic bezier path through all points ---
            fun buildSmoothPath(): Path =
                Path().apply {
                    if (pts.size == 1) {
                        moveTo(pts[0].x, pts[0].y)
                        return@apply
                    }
                    moveTo(pts[0].x, pts[0].y)
                    for (i in 0 until pts.size - 1) {
                        val p0 = if (i > 0) pts[i - 1] else pts[i]
                        val p1 = pts[i]
                        val p2 = pts[i + 1]
                        val p3 = if (i < pts.size - 2) pts[i + 2] else pts[i + 1]
                        val cp1x = p1.x + (p2.x - p0.x) / 6f
                        val cp1y = p1.y + (p2.y - p0.y) / 6f
                        val cp2x = p2.x - (p3.x - p1.x) / 6f
                        val cp2y = p2.y - (p3.y - p1.y) / 6f
                        cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                    }
                }

            // Fill under curve
            paint.style = Paint.Style.FILL
            paint.color = fillColorInt
            val fillPath =
                buildSmoothPath().apply {
                    lineTo(pts.last().x, height - pad)
                    lineTo(pts.first().x, height - pad)
                    close()
                }
            canvas.drawPath(fillPath, paint)

            // Stroke the smooth line
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 6f
            paint.color = colorInt
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeJoin = Paint.Join.ROUND
            canvas.drawPath(buildSmoothPath(), paint)

            // Smaller dots
            paint.style = Paint.Style.FILL
            val dotRadius = 5f
            paint.strokeWidth = 0f
            pts.forEach { canvas.drawCircle(it.x, it.y, dotRadius, paint) }

            bmp
        }

    Image(
        provider = ImageProvider(bitmap),
        contentDescription = "Progress Line Graph",
        contentScale = ContentScale.FillBounds,
        modifier = modifier.fillMaxWidth().height(graphHeight),
    )
}

private data class Offset(val x: Float, val y: Float)
