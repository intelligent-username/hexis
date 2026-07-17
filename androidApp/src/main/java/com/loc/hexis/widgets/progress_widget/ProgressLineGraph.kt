package com.loc.hexis.widgets.progress_widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
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
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.unit.ColorProvider
import kotlin.math.max
import kotlin.math.min
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

    val bitmap =
        remember(data, colorInt) {
            val w = 1200
            val h = 360
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            val maxVal = max(data.maxOrNull() ?: 1, 1)
            val minVal = min(data.minOrNull() ?: 0, 0)
            val range = max(maxVal - minVal, 1)

            val padL = 40f
            val padR = 24f
            val padT = 24f
            val padB = 28f
            val gW = w - padL - padR
            val gH = h - padT - padB

            // --- points ---
            val pts =
                data.mapIndexed { i, v ->
                    val x = padL + (i.toFloat() / (data.size - 1).coerceAtLeast(1)) * gW
                    val y = h - padB - ((v - minVal).toFloat() / range) * gH
                    Offset(x, y)
                }

            // --- subtle horizontal grid lines ---
            paint.color = colorInt and 0x18FFFFFF
            paint.strokeWidth = 1f
            paint.style = Paint.Style.STROKE
            val gridCount = 3
            for (i in 1..gridCount) {
                val y = padT + (gH / (gridCount + 1)) * i
                canvas.drawLine(padL, y, w - padR, y, paint)
            }

            // --- smooth cubic bezier path ---
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

            // --- fill ---
            paint.style = Paint.Style.FILL
            paint.color = colorInt and 0x14FFFFFF
            val fillPath =
                Path(smoothPath).apply {
                    lineTo(pts.last().x, h - padB)
                    lineTo(pts.first().x, h - padB)
                    close()
                }
            canvas.drawPath(fillPath, paint)

            // --- glow (wide translucent stroke) ---
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 14f
            paint.color = colorInt and 0x18FFFFFF
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeJoin = Paint.Join.ROUND
            canvas.drawPath(smoothPath, paint)

            // --- main line ---
            paint.strokeWidth = 5f
            paint.color = colorInt
            canvas.drawPath(smoothPath, paint)

            // --- tiny dots ---
            paint.style = Paint.Style.FILL
            paint.strokeWidth = 0f
            val dotR = 10f
            paint.color = colorInt and 0xCCFFFFFF.toInt()
            pts.forEach { canvas.drawCircle(it.x, it.y, dotR, paint) }
            paint.color = -0x1 // white outline dot
            pts.forEach { canvas.drawCircle(it.x, it.y, dotR * 0.4f, paint) }

            // --- last value label ---
            paint.style = Paint.Style.FILL
            paint.color = colorInt
            paint.textSize = 22f
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.isAntiAlias = true
            val lastLabel = "${data.last()}"
            val labelW = paint.measureText(lastLabel)
            val lastPt = pts.last()
            val labelX = if (lastPt.x + labelW + 10f <= w - padR) {
                lastPt.x + 10f
            } else {
                lastPt.x - labelW - 10f
            }
            val labelY = lastPt.y - 6f
            // subtle bg behind label
            paint.style = Paint.Style.FILL
            paint.color = -0x1 // white bg
            val bgPad = 6f
            canvas.drawRoundRect(
                labelX - bgPad, labelY - paint.textSize + 2f,
                labelX + labelW + bgPad, labelY + 4f,
                6f, 6f, paint,
            )
            paint.color = colorInt
            canvas.drawText(lastLabel, labelX, labelY, paint)

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
