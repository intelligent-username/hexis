/*
 * Copyright (C) 2025 Hexis Contributors
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.loc.hexis.widgets.progress_widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.unit.ColorProvider
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Bar+line hybrid chart.
 *
 * [dailyData]   — 14 daily point values for the trend line
 * [weeklyData]  — per-week point totals for bar columns (optional, up to 8 bars)
 * [bestWeek]    — all-time best week, used to scale bars (0 = auto-scale from data)
 * [currentPartial] — current partial-week points; if provided, last bar renders as a
 *                    semi-transparent "in progress" column
 */
@Composable
fun ProgressLineGraph(
    dailyData: List<Int>,
    weeklyData: List<Int> = emptyList(),
    bestWeek: Int = 0,
    currentPartial: Int = 0,
    modifier: GlanceModifier = GlanceModifier,
    graphHeight: Dp = 80.dp,
    lineColor: ColorProvider = GlanceTheme.colors.primary,
    surfaceColor: ColorProvider = GlanceTheme.colors.widgetBackground,
) {
    val data = dailyData.takeLast(14)
    if (data.size < 2) return

    val context = LocalContext.current
    val size = LocalSize.current
    val resolvedLine = lineColor.getColor(context)
    val resolvedSurface = surfaceColor.getColor(context)

    fun colorToInt(c: androidx.compose.ui.graphics.Color): Int =
        ((c.alpha * 255f).roundToInt() shl 24) or
            ((c.red * 255f).roundToInt() shl 16) or
            ((c.green * 255f).roundToInt() shl 8) or
            ((c.blue * 255f).roundToInt())

    val lineColorInt = colorToInt(resolvedLine)
    val surfaceColorInt = colorToInt(resolvedSurface)

    val bars = weeklyData.takeLast(8)
    val hasWeekly = bars.size >= 2

    // Calculate actual pixel dimensions using screen density for sharp 1:1 rendering
    val density = context.resources.displayMetrics.density
    val scale = 1.2f
    val w = max((size.width.value * density * scale).roundToInt(), 120)
    val h = max((graphHeight.value * density * scale).roundToInt(), 60)

    val bitmap =
        remember(data, bars, lineColorInt, currentPartial, bestWeek, w, h) {
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            
            // Scale canvas from virtual 1200x400 space to physical pixels
            val sx = w.toFloat() / 1200f
            val sy = h.toFloat() / 400f
            canvas.scale(sx, sy)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Dynamic padding inside the 1200x400 virtual space:
            // Top padding (90f) leaves ample space for the overlaid Header.
            // Bottom padding (85f) leaves space for the overlaid Footer.
            val padL = 8f
            val padR = 8f
            val padT = 90f
            val padB = 85f
            val gW = 1200f - padL - padR
            val gH = 400f - padT - padB
            val barMax = max(
                if (bestWeek > 0) bestWeek else bars.maxOrNull() ?: 1,
                1
            )
            val barCount = bars.size
            val totalGap = gW * 0.25f
            val gap = totalGap / (barCount + 1)
            val barW = (gW - totalGap) / barCount

            // Calculate trend line points matching the center top of each weekly bar
            val pts = bars.mapIndexed { i, valPoints ->
                val x = padL + gap * (i + 1) + barW * i
                val barCenterX = x + barW / 2f
                val frac = (valPoints.toFloat() / barMax).coerceIn(0f, 1f)
                val y = 400f - padB - frac * gH
                Offset(barCenterX, y)
            }

            // ── BAR COLUMNS ────────────────────────────────────────────────
            bars.forEachIndexed { i, valPoints ->
                val isLast = i == barCount - 1 && currentPartial > 0
                val frac = (valPoints.toFloat() / barMax).coerceIn(0f, 1f)
                val barH = (frac * gH).coerceAtLeast(4f)
                val x = padL + gap * (i + 1) + barW * i
                val top = 400f - padB - barH
                val rect = RectF(x, top, x + barW, 400f - padB)
                val radius = barW * 0.25f

                if (isLast) {
                    // In-progress: striped / translucent with dashed border
                    paint.color = lineColorInt and 0x28FFFFFF.toInt()
                    paint.style = Paint.Style.FILL
                    canvas.drawRoundRect(rect, radius, radius, paint)
                    paint.color = lineColorInt and 0x60FFFFFF.toInt()
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 3f
                    paint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(12f, 8f), 0f)
                    canvas.drawRoundRect(rect, radius, radius, paint)
                    paint.pathEffect = null
                } else {
                    // Solid bar with vertical gradient from primary to primary@40%
                    val gradient = LinearGradient(
                        x, top, x, 400f - padB,
                        intArrayOf(
                            lineColorInt and 0x90FFFFFF.toInt(),
                            lineColorInt and 0x30FFFFFF.toInt(),
                        ),
                        null,
                        Shader.TileMode.CLAMP,
                    )
                    paint.shader = gradient
                    paint.style = Paint.Style.FILL
                    canvas.drawRoundRect(rect, radius, radius, paint)
                    paint.shader = null
                }
            }

            // ── TREND LINE (smooth curve overlay) ──────────────────────────
            fun buildSmoothPath(): Path =
                Path().apply {
                    moveTo(pts[0].x, pts[0].y)
                    for (i in 0 until pts.size - 1) {
                        val p0 = if (i > 0) pts[i - 1] else pts[i]
                        val p1 = pts[i]
                        val p2 = pts[i + 1]
                        val p3 = if (i < pts.size - 2) pts[i + 2] else pts[i + 1]
                        cubicTo(
                            p1.x + (p2.x - p0.x) / 6f,
                            p1.y + (p2.y - p0.y) / 6f,
                            p2.x - (p3.x - p1.x) / 6f,
                            p2.y - (p3.y - p1.y) / 6f,
                            p2.x, p2.y,
                        )
                    }
                }

            val smoothPath = buildSmoothPath()

            // Subtle area fill under line
            paint.style = Paint.Style.FILL
            val areaGradient = LinearGradient(
                0f, padT, 0f, 400f - padB,
                intArrayOf(
                    lineColorInt and 0x22FFFFFF.toInt(),
                    lineColorInt and 0x00FFFFFF.toInt(),
                ),
                null,
                Shader.TileMode.CLAMP,
            )
            paint.shader = areaGradient
            val fillPath = Path(smoothPath).apply {
                lineTo(pts.last().x, 400f - padB)
                lineTo(pts.first().x, 400f - padB)
                close()
            }
            canvas.drawPath(fillPath, paint)
            paint.shader = null

            // Subtle horizontal baseline
            paint.color = lineColorInt and 0x24FFFFFF.toInt()
            paint.strokeWidth = 2f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(padL, 400f - padB, 1200f - padR, 400f - padB, paint)

            // Main line — clean, no glow
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 6f
            paint.color = lineColorInt
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeJoin = Paint.Join.ROUND
            canvas.drawPath(smoothPath, paint)

            // Accent dot on last point only (center of current week's bar)
            val last = pts.last()
            paint.style = Paint.Style.FILL
            paint.color = surfaceColorInt
            canvas.drawCircle(last.x, last.y, 14f, paint)
            paint.color = lineColorInt
            canvas.drawCircle(last.x, last.y, 9f, paint)

            // Last-point value label — floats above the dot
            paint.textSize = 28f
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.isAntiAlias = true
            paint.color = lineColorInt
            val lastLabel = "${bars.last()}"
            val labelW = paint.measureText(lastLabel)
            val labelX = (last.x - labelW / 2f).coerceIn(padL, 1200f - padR - labelW)
            val labelY = (last.y - 22f).coerceAtLeast(padT + paint.textSize)
            canvas.drawText(lastLabel, labelX, labelY, paint)

            bmp
        }

    Image(
        provider = ImageProvider(bitmap),
        contentDescription = "Progress chart",
        contentScale = ContentScale.FillBounds,
        modifier = modifier.fillMaxWidth().height(graphHeight),
    )
}

private data class Offset(val x: Float, val y: Float)
