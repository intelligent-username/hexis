package com.loc.hexis.shared.ui.note.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.loc.hexis.shared.ui.note.LineType
import com.loc.hexis.shared.ui.note.parseContentLines

class NoteVisualTransformation(
    private val primaryColor: Color = Color(0xFF6750A4),
    private val mutedColor: Color = Color(0xFF79747E),
    private val ruleColor: Color = Color(0xFFE7E0EC),
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val lines = parseContentLines(original)
        val builder = AnnotatedString.Builder()
        val outToOrig = mutableListOf<Int>()

        var origPos = 0
        for ((li, line) in lines.withIndex()) {
            if (li > 0) {
                builder.append('\n')
                outToOrig.add(origPos)
            }

            val rawLineStart = origPos
            val rawLineEnd = original.indexOf('\n', origPos).let { if (it == -1) original.length else it }
            val rawLine = original.substring(rawLineStart, rawLineEnd)
            val trimmed = rawLine.trimStart()
            val indent = rawLine.length - trimmed.length

            when (line.type) {
                LineType.HEADER -> {
                    origPos += indent + 2
                    pushHeaderStyle(builder, 22.sp, FontWeight.ExtraBold)
                    appendContent(line.text, builder, outToOrig, origPos)
                    builder.pop()
                    origPos += line.text.length
                }

                LineType.SUB_HEADER -> {
                    origPos += indent + 3
                    pushHeaderStyle(builder, 18.sp, FontWeight.Bold)
                    appendContent(line.text, builder, outToOrig, origPos)
                    builder.pop()
                    origPos += line.text.length
                }

                LineType.SUB_SUB_HEADER -> {
                    origPos += indent + 4
                    pushHeaderStyle(builder, 16.sp, FontWeight.SemiBold)
                    appendContent(line.text, builder, outToOrig, origPos)
                    builder.pop()
                    origPos += line.text.length
                }

                LineType.BULLET_LIST -> {
                    origPos += indent + 2
                    builder.pushStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.ExtraBold))
                    builder.append('\u2022')
                    outToOrig.add((origPos - 2).coerceAtLeast(0))
                    builder.append(' ')
                    outToOrig.add((origPos - 1).coerceAtLeast(0))
                    builder.pop()
                    appendContent(line.text, builder, outToOrig, origPos)
                    origPos += line.text.length
                }

                LineType.NUMBERED_LIST -> {
                    val numStr = "${line.number ?: 1}."
                    val firstNonSpace = rawLine.indexOfFirst { it != ' ' && it != '\t' }
                    val spaceAfterNum = if (firstNonSpace != -1) rawLine.indexOf(' ', firstNonSpace) else -1
                    val prefixEnd = if (spaceAfterNum != -1) spaceAfterNum + 1 else indent + 3
                    origPos += prefixEnd

                    builder.pushStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.ExtraBold))
                    builder.append("$numStr ")
                    repeat(numStr.length + 1) { idx ->
                        outToOrig.add((origPos - (numStr.length + 1) + idx).coerceAtLeast(0))
                    }
                    builder.pop()
                    appendContent(line.text, builder, outToOrig, origPos)
                    origPos += line.text.length
                }

                LineType.QUOTE -> {
                    origPos += indent + 2
                    builder.pushStyle(SpanStyle(color = primaryColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold))
                    builder.append('\u2502')
                    outToOrig.add((origPos - 2).coerceAtLeast(0))
                    builder.append(' ')
                    outToOrig.add((origPos - 1).coerceAtLeast(0))
                    builder.pop()
                    builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = primaryColor))
                    appendContent(line.text, builder, outToOrig, origPos)
                    builder.pop()
                    origPos += line.text.length
                }

                LineType.HORIZONTAL_RULE -> {
                    origPos += rawLine.length
                    builder.pushStyle(
                        SpanStyle(
                            color = primaryColor.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Normal,
                            background = primaryColor.copy(alpha = 0.1f),
                        )
                    )
                    val dashLine = "\u2015".repeat(30)
                    builder.append(dashLine)
                    repeat(dashLine.length) { outToOrig.add((origPos - 1).coerceAtLeast(0)) }
                    builder.pop()
                }

                LineType.CHECKLIST -> {
                    val prefixLen = rawLine.length - line.text.length
                    origPos += prefixLen
                    builder.pushStyle(SpanStyle(color = primaryColor))
                    val checkbox = if (line.isChecked) "\u2611" else "\u2610"
                    builder.append("$checkbox ")
                    repeat(2) { outToOrig.add((origPos - prefixLen).coerceAtLeast(0)) }
                    builder.pop()
                    appendContent(line.text, builder, outToOrig, origPos)
                    origPos += line.text.length
                }

                LineType.REGULAR -> {
                    for (ch in rawLine) {
                        builder.append(ch)
                        outToOrig.add(origPos)
                        origPos++
                    }
                }
            }

            if (origPos < original.length && original[origPos] == '\n') {
                origPos++
            }
        }

        return TransformedText(
            builder.toAnnotatedString(),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (outToOrig.isEmpty()) return 0
                    val idx = outToOrig.indexOfFirst { it >= offset }
                    return if (idx == -1) outToOrig.size else idx
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (outToOrig.isEmpty()) return 0
                    return outToOrig.getOrElse(offset) { outToOrig.last() }
                }
            },
        )
    }

    private fun pushHeaderStyle(
        builder: AnnotatedString.Builder,
        fontSize: androidx.compose.ui.unit.TextUnit,
        fontWeight: FontWeight,
    ) {
        builder.pushStyle(
            SpanStyle(
                fontWeight = fontWeight,
                fontSize = fontSize,
                color = primaryColor,
            )
        )
    }

    private fun appendContent(
        text: String,
        builder: AnnotatedString.Builder,
        outToOrig: MutableList<Int>,
        origPos: Int,
    ) {
        for (i in text.indices) {
            builder.append(text[i])
            outToOrig.add(origPos + i)
        }
    }
}
