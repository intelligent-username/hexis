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

        val builder = AnnotatedString.Builder(original)
        var lineStartPos = 0

        original.lines().forEach { line ->
            val lineEndPos = lineStartPos + line.length
            val trimmed = line.trimStart()
            val indent = line.length - trimmed.length
            val prefixStart = lineStartPos + indent

            when {
                trimmed.startsWith("### ") -> {
                    // Sub-sub header (### )
                    builder.addStyle(
                        SpanStyle(color = mutedColor, fontWeight = FontWeight.Bold),
                        prefixStart,
                        prefixStart + 4,
                    )
                    builder.addStyle(
                        SpanStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = primaryColor),
                        prefixStart + 4,
                        lineEndPos,
                    )
                }
                trimmed.startsWith("## ") -> {
                    // Sub header (## )
                    builder.addStyle(
                        SpanStyle(color = mutedColor, fontWeight = FontWeight.Bold),
                        prefixStart,
                        prefixStart + 3,
                    )
                    builder.addStyle(
                        SpanStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold, color = primaryColor),
                        prefixStart + 3,
                        lineEndPos,
                    )
                }
                trimmed.startsWith("# ") -> {
                    // Header (# )
                    builder.addStyle(
                        SpanStyle(color = mutedColor, fontWeight = FontWeight.Bold),
                        prefixStart,
                        prefixStart + 2,
                    )
                    builder.addStyle(
                        SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = primaryColor),
                        prefixStart + 2,
                        lineEndPos,
                    )
                }
                trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                    // Bullet List (* or - )
                    builder.addStyle(
                        SpanStyle(color = primaryColor, fontWeight = FontWeight.ExtraBold),
                        prefixStart,
                        prefixStart + 2,
                    )
                }
                trimmed.startsWith("> ") -> {
                    // Quote (> )
                    builder.addStyle(
                        SpanStyle(color = primaryColor.copy(alpha = 0.6f), fontWeight = FontWeight.Bold),
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
                    // Numbered List (1. or 1) )
                    val spaceIdx = line.indexOf(' ', indent)
                    if (spaceIdx != -1 && spaceIdx < lineEndPos) {
                        builder.addStyle(
                            SpanStyle(color = primaryColor, fontWeight = FontWeight.ExtraBold),
                            prefixStart,
                            lineStartPos + spaceIdx + 1,
                        )
                    }
                }
                trimmed.matches(Regex("""^[-*_]{3,}\s*$""")) -> {
                    // Horizontal Rule (---)
                    builder.addStyle(
                        SpanStyle(
                            color = primaryColor.copy(alpha = 0.5f),
                            textDecoration = TextDecoration.LineThrough,
                            fontWeight = FontWeight.Bold,
                        ),
                        prefixStart,
                        lineEndPos,
                    )
                }
            }

            lineStartPos = lineEndPos + 1 // +1 for '\n'
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
