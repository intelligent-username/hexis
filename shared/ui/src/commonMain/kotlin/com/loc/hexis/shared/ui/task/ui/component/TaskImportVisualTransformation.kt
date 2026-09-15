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

package com.loc.hexis.shared.ui.task.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class TaskImportVisualTransformation(
    private val primaryColor: Color,
    private val onSurfaceColor: Color,
    private val onSurfaceVariantColor: Color,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val builder = AnnotatedString.Builder(text)
        val highlightColor = primaryColor.copy(alpha = 0.08f)

        var lineStart = 0
        while (lineStart < raw.length) {
            var lineEnd = raw.indexOf('\n', lineStart)
            if (lineEnd == -1) lineEnd = raw.length

            val line = raw.substring(lineStart, lineEnd)
            val trimmed = line.trimStart()
            val leadingSpaces = line.length - trimmed.length

            if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                val bulletIndex = lineStart + leadingSpaces
                builder.addStyle(
                    SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold),
                    bulletIndex,
                    bulletIndex + 1,
                )
                if (bulletIndex + 1 < lineEnd) {
                    builder.addStyle(
                        SpanStyle(color = onSurfaceColor, fontWeight = FontWeight.Bold),
                        bulletIndex + 1,
                        lineEnd,
                    )
                }
            } else if (trimmed.startsWith(">")) {
                val prefixIndex = lineStart + leadingSpaces
                builder.addStyle(
                    SpanStyle(background = highlightColor),
                    prefixIndex,
                    lineEnd,
                )
                builder.addStyle(
                    SpanStyle(
                        color = primaryColor.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold,
                    ),
                    prefixIndex,
                    prefixIndex + 1,
                )
                if (prefixIndex + 1 < lineEnd) {
                    builder.addStyle(
                        SpanStyle(color = onSurfaceVariantColor),
                        prefixIndex + 1,
                        lineEnd,
                    )
                }
            }

            lineStart = lineEnd + 1
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
