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

package com.loc.hexis.shared.ui.note.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.loc.hexis.shared.ui.note.LineType
import com.loc.hexis.shared.ui.note.parseContentLines

/**
 * Hides markdown prefixes and applies line-level rich text styling.
 * The user sees the rendered result; the underlying storage is markdown.
 */
class NoteVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val lines = parseContentLines(original)
        val builder = AnnotatedString.Builder()

        val origLen = original.length
        val origToOut = IntArray(origLen) { -1 }
        val outToOrig = mutableListOf<Int>()

        var ip = 0 // index in original
        var op = 0 // index in output (builder)

        for ((li, line) in lines.withIndex()) {
            // ---- newline before this line (except first) ----
            if (li > 0) {
                builder.append('\n')
                // map the output newline to the original newline
                outToOrig.add(ip)
                if (ip < origLen && original[ip] == '\n') {
                    origToOut[ip] = op
                    ip++
                }
                op++
            }

            // ---- extract original line text ----
            val lineStart = ip
            var origLineEnd = ip
            while (origLineEnd < origLen && original[origLineEnd] != '\n') origLineEnd++
            val origLine = original.substring(lineStart, origLineEnd)

            val trimmed = origLine.trimStart()
            val indentLen = origLine.length - trimmed.length

            // ---- compute hidden prefix length ----
            val prefixLen = when {
                trimmed.startsWith("### ") -> trimmed.indexOf("### ") + 4
                trimmed.startsWith("## ") -> trimmed.indexOf("## ") + 3
                trimmed.startsWith("# ") -> trimmed.indexOf("# ") + 2
                trimmed.startsWith("* ") -> trimmed.indexOf("* ") + 2
                trimmed.startsWith("- ") -> trimmed.indexOf("- ") + 2
                numberedRegex.matches(trimmed) -> {
                    val m = numberedRegex.find(trimmed)!!
                    trimmed.indexOf(m.value) + m.value.length
                }
                else -> 0
            }

            // ---- skip leading whitespace (hidden) ----
            ip += indentLen
            // ---- skip prefix (hidden) ----
            ip += prefixLen

            // ---- visual prefix (bullet/number) ----
            val contentText = line.text
            val isListItem = line.type == LineType.BULLET_LIST || line.type == LineType.NUMBERED_LIST

            // Tighter lineHeight for wrapped lines within the same list item
            if (isListItem) {
                builder.pushStyle(ParagraphStyle(lineHeight = 18.sp))
            }

            val visualPrefix = when (line.type) {
                LineType.BULLET_LIST -> "  \u2022  "
                LineType.NUMBERED_LIST -> "${line.number ?: 1}.  "
                else -> ""
            }

            if (visualPrefix.isNotEmpty()) {
                builder.append(visualPrefix)
                for (i in visualPrefix.indices) {
                    // visual prefix chars don't exist in original → map to line start in original
                    val mapTo = (ip - contentText.length).coerceAtLeast(lineStart)
                    outToOrig.add(mapTo)
                }
                op += visualPrefix.length
            }

            // ---- styled content text ----
            val style = when (line.type) {
                LineType.HEADER -> SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                LineType.SUB_HEADER -> SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                LineType.SUB_SUB_HEADER -> SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
                LineType.BULLET_LIST -> SpanStyle(fontSize = 14.sp)
                LineType.NUMBERED_LIST -> SpanStyle(fontSize = 14.sp)
                LineType.REGULAR -> SpanStyle(fontSize = 14.sp)
            }

            builder.pushStyle(style)
            builder.append(contentText)
            builder.pop()
            if (isListItem) builder.pop() // pop ParagraphStyle
            for (i in contentText.indices) {
                val origIdx = ip + i
                if (origIdx < origLen) {
                    origToOut[origIdx] = op
                    outToOrig.add(origIdx)
                }
                op++
            }
            ip += contentText.length
        }

        val annotated = builder.toAnnotatedString()

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= origLen) return annotated.length
                val t = origToOut[offset]
                if (t >= 0) return t
                // hidden char — find next visible
                var next = offset + 1
                while (next < origLen && origToOut[next] < 0) next++
                return if (next >= origLen) annotated.length else origToOut[next]
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= outToOrig.size) return origLen
                return outToOrig[offset]
            }
        }

        return TransformedText(annotated, mapping)
    }
}

private val numberedRegex = Regex("""^(\d+)[\.\)]\s""")
