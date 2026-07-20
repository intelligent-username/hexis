package com.loc.hexis.shared.ui.note.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

class NoteVisualTransformation(
    private val primaryColor: Color = Color(0xFF6750A4),
    private val mutedColor: Color = Color(0xFF79747E),
    private val ruleColor: Color = Color(0xFFE7E0EC),
    private val collapsedLineIndices: Set<Int> = emptySet(),
) : VisualTransformation {

    private data class HeaderInfo(val lineIndex: Int, val level: Int)

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val lines = original.lines()
        val headers = mutableListOf<HeaderInfo>()
        lines.forEachIndexed { index, line ->
            val trimmed = line.trimStart()
            val level =
                when {
                    trimmed.startsWith("# ") -> 1
                    trimmed.startsWith("## ") -> 2
                    trimmed.startsWith("### ") -> 3
                    else -> 0
                }
            if (level > 0) {
                headers.add(HeaderInfo(index, level))
            }
        }

        val collapsedRanges = mutableMapOf<Int, Int>()
        collapsedLineIndices.forEach { hIndex ->
            val header = headers.firstOrNull { it.lineIndex == hIndex }
            if (header != null) {
                val nextHeader =
                    headers.firstOrNull { it.lineIndex > hIndex && it.level <= header.level }
                val endLine = (nextHeader?.lineIndex ?: lines.size) - 1
                if (endLine > hIndex) {
                    collapsedRanges[hIndex] = endLine
                }
            }
        }

        val hiddenLines = mutableSetOf<Int>()
        collapsedRanges.forEach { (startLine, endLine) ->
            for (i in (startLine + 1)..endLine) {
                hiddenLines.add(i)
            }
        }

        val builder = AnnotatedString.Builder()
        val origToTrans = IntArray(original.length + 1)
        val transToOrig = mutableListOf<Int>()

        var origPos = 0
        var currentTransformedLen = 0

        var lineIdx = 0
        while (lineIdx < lines.size) {
            val line = lines[lineIdx]
            val lineLen = line.length

            if (lineIdx in collapsedRanges) {
                val trimmed = line.trimStart()
                val indent = line.length - trimmed.length
                val headerPrefixLen = when {
                    trimmed.startsWith("# ") -> 2
                    trimmed.startsWith("## ") -> 3
                    trimmed.startsWith("### ") -> 4
                    else -> 0
                }

                if (indent > 0) {
                    val indentStr = line.substring(0, indent)
                    builder.append(indentStr)
                    for (chIdx in 0 until indent) {
                        if (origPos + chIdx <= original.length) {
                            origToTrans[origPos + chIdx] = currentTransformedLen + chIdx
                        }
                        transToOrig.add(origPos + chIdx)
                    }
                    currentTransformedLen += indent
                    origPos += indent
                }

                val hashtagStartTrans = currentTransformedLen
                for (chIdx in 0 until headerPrefixLen) {
                    if (origPos + chIdx <= original.length) {
                        origToTrans[origPos + chIdx] = hashtagStartTrans
                    }
                }
                origPos += headerPrefixLen

                val bodyText = line.substring(indent + headerPrefixLen)
                val bodyStartTrans = currentTransformedLen
                builder.append(bodyText)
                for (chIdx in 0 until bodyText.length) {
                    if (origPos + chIdx <= original.length) {
                        origToTrans[origPos + chIdx] = bodyStartTrans + chIdx
                    }
                    transToOrig.add(origPos + chIdx)
                }
                currentTransformedLen += bodyText.length
                origPos += bodyText.length

                val level = when {
                    trimmed.startsWith("# ") -> 1
                    trimmed.startsWith("## ") -> 2
                    else -> 3
                }
                applyHeaderStyle(builder, level, bodyStartTrans, currentTransformedLen)

                if (lineIdx < lines.size - 1) {
                    builder.append("\n")
                    if (origPos <= original.length) {
                        origToTrans[origPos] = currentTransformedLen
                    }
                    transToOrig.add(origPos)
                    origPos += 1
                    currentTransformedLen += 1
                }

                val endLine = collapsedRanges[lineIdx]!!
                val hiddenCount = endLine - lineIdx
                var hiddenChars = 0
                for (h in (lineIdx + 1)..endLine) {
                    hiddenChars += lines[h].length + 1
                }

                val placeholder = "  \u2026 $hiddenCount line${if (hiddenCount > 1) "s" else ""} hidden"
                val placeholderStart = currentTransformedLen
                builder.append(placeholder)
                builder.addStyle(
                    SpanStyle(color = mutedColor, fontStyle = FontStyle.Italic, fontSize = 14.sp),
                    placeholderStart,
                    placeholderStart + placeholder.length,
                )

                for (chIdx in 0 until hiddenChars) {
                    if (origPos + chIdx <= original.length) {
                        origToTrans[origPos + chIdx] = placeholderStart
                    }
                }
                repeat(placeholder.length) { transToOrig.add(origPos) }
                currentTransformedLen += placeholder.length
                origPos += hiddenChars

                if (endLine < lines.size - 1) {
                    builder.append("\n")
                    if (origPos <= original.length) {
                        origToTrans[origPos] = currentTransformedLen
                    }
                    transToOrig.add(origPos.coerceAtMost(original.length))
                    currentTransformedLen += 1
                }

                lineIdx = endLine + 1
            } else if (lineIdx in hiddenLines) {
                lineIdx++
            } else {
                val trimmed = line.trimStart()
                val indent = line.length - trimmed.length
                val headerPrefixLen = when {
                    trimmed.startsWith("# ") -> 2
                    trimmed.startsWith("## ") -> 3
                    trimmed.startsWith("### ") -> 4
                    else -> 0
                }

                if (headerPrefixLen > 0) {
                    if (indent > 0) {
                        val indentStr = line.substring(0, indent)
                        builder.append(indentStr)
                        for (chIdx in 0 until indent) {
                            if (origPos + chIdx <= original.length) {
                                origToTrans[origPos + chIdx] = currentTransformedLen + chIdx
                            }
                            transToOrig.add(origPos + chIdx)
                        }
                        currentTransformedLen += indent
                        origPos += indent
                    }

                    val hashtagStartTrans = currentTransformedLen
                    for (chIdx in 0 until headerPrefixLen) {
                        if (origPos + chIdx <= original.length) {
                            origToTrans[origPos + chIdx] = hashtagStartTrans
                        }
                    }
                    origPos += headerPrefixLen

                    val bodyText = line.substring(indent + headerPrefixLen)
                    val bodyStartTrans = currentTransformedLen
                    builder.append(bodyText)
                    for (chIdx in 0 until bodyText.length) {
                        if (origPos + chIdx <= original.length) {
                            origToTrans[origPos + chIdx] = bodyStartTrans + chIdx
                        }
                        transToOrig.add(origPos + chIdx)
                    }
                    currentTransformedLen += bodyText.length
                    origPos += bodyText.length

                    val level = when {
                        trimmed.startsWith("# ") -> 1
                        trimmed.startsWith("## ") -> 2
                        else -> 3
                    }
                    applyHeaderStyle(builder, level, bodyStartTrans, currentTransformedLen)
                } else {
                    val startLineTrans = currentTransformedLen
                    val isRule = trimmed.matches(Regex("""^[-*_]{3,}\s*$"""))
                    val displayLine = if (isRule) {
                        line.replace('-', '\u2501').replace('*', '\u2501').replace('_', '\u2501')
                    } else {
                        line
                    }
                    builder.append(displayLine)
                    for (chIdx in 0 until lineLen) {
                        if (origPos + chIdx <= original.length) {
                            origToTrans[origPos + chIdx] = startLineTrans + chIdx
                        }
                        transToOrig.add(origPos + chIdx)
                    }
                    currentTransformedLen += lineLen
                    origPos += lineLen

                    applyLineStyles(line, builder, startLineTrans, currentTransformedLen)
                }

                if (lineIdx < lines.size - 1) {
                    builder.append("\n")
                    if (origPos <= original.length) {
                        origToTrans[origPos] = currentTransformedLen
                    }
                    transToOrig.add(origPos)
                    origPos += 1
                    currentTransformedLen += 1
                }

                lineIdx++
            }
        }

        if (original.length < origToTrans.size) {
            origToTrans[original.length] = currentTransformedLen
        }
        transToOrig.add(original.length)

        val transToOrigArray = transToOrig.toIntArray()

        val offsetMapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return origToTrans[offset.coerceIn(0, original.length)]
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return transToOrigArray[offset.coerceIn(0, transToOrigArray.size - 1)]
                }
            }

        return TransformedText(builder.toAnnotatedString(), offsetMapping)
    }

    private fun applyHeaderStyle(
        builder: AnnotatedString.Builder,
        level: Int,
        start: Int,
        end: Int,
    ) {
        when (level) {
            1 ->
                builder.addStyle(
                    SpanStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor,
                    ),
                    start,
                    end,
                )
            2 ->
                builder.addStyle(
                    SpanStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                    ),
                    start,
                    end,
                )
            3 ->
                builder.addStyle(
                    SpanStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor,
                    ),
                    start,
                    end,
                )
        }
    }

    private fun applyLineStyles(
        line: String,
        builder: AnnotatedString.Builder,
        lineStartPos: Int,
        lineEndPos: Int,
    ) {
        val trimmed = line.trimStart()
        val indent = line.length - trimmed.length
        val prefixStart = lineStartPos + indent

        when {
            trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                builder.addStyle(
                    SpanStyle(color = primaryColor, fontWeight = FontWeight.ExtraBold),
                    prefixStart,
                    prefixStart + 2,
                )
            }
            trimmed.startsWith("> ") -> {
                builder.addStyle(
                    SpanStyle(
                        color = primaryColor.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                    ),
                    prefixStart,
                    prefixStart + 2,
                )
                builder.addStyle(
                    SpanStyle(fontStyle = FontStyle.Italic, color = primaryColor),
                    prefixStart + 2,
                    lineEndPos,
                )
            }
            trimmed.matches(Regex("""^(\d+)[\.\)]\s.*""")) -> {
                val spaceIdx = line.indexOf(' ', indent)
                if (spaceIdx != -1 && spaceIdx < line.length) {
                    builder.addStyle(
                        SpanStyle(color = primaryColor, fontWeight = FontWeight.ExtraBold),
                        prefixStart,
                        lineStartPos + spaceIdx + 1,
                    )
                }
            }
            trimmed.matches(Regex("""^[-*_]{3,}\s*$""")) -> {
                builder.addStyle(
                    SpanStyle(
                        color = primaryColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                    ),
                    prefixStart,
                    lineEndPos,
                )
            }
        }
    }
}
