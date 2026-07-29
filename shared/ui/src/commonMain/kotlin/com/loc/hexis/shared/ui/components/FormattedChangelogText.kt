package com.loc.hexis.shared.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun FormattedChangelogItem(
    text: String,
    modifier: Modifier = Modifier,
) {
    val lines = text.split("\n")
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        lines.forEach { rawLine ->
            if (rawLine.isBlank()) return@forEach

            val leadingSpaces = rawLine.takeWhile { it == ' ' || it == '\t' }.length
            val indentLevel = (leadingSpaces / 2).coerceAtMost(3)
            val trimmedLine = rawLine.trimStart()

            val (bullet, contentLine) =
                when {
                    trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") -> {
                        val bulletChar =
                            when (indentLevel) {
                                0 -> "•"
                                1 -> "◦"
                                else -> "▪"
                            }
                        bulletChar to trimmedLine.substring(2)
                    }
                    trimmedLine.startsWith("+ ") -> "•" to trimmedLine.substring(2)
                    trimmedLine.matches(Regex("""^\d+\.\s+.*""")) -> {
                        val num = trimmedLine.substringBefore(".")
                        "$num." to trimmedLine.substringAfter(".").trimStart()
                    }
                    else -> null to trimmedLine
                }

            Row(
                modifier =
                    Modifier.fillMaxWidth().padding(start = (indentLevel * 14).dp),
                verticalAlignment = Alignment.Top,
            ) {
                if (bullet != null) {
                    Text(
                        text = bullet,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            ),
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }

                Text(
                    text = parseInlineMarkdown(contentLine),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        }
    }
}

fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val regex = Regex("""(\*\*(.*?)\*\*|__(.*?)__|`([^`]+)`|\*(.*?)\*|_(.*?)_)""")
        var lastIndex = 0
        for (match in regex.findAll(text)) {
            val start = match.range.first
            val end = match.range.last + 1
            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }
            val matchValue = match.value
            when {
                matchValue.startsWith("**") || matchValue.startsWith("__") -> {
                    val inner = matchValue.substring(2, matchValue.length - 2)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(inner)
                    }
                }
                matchValue.startsWith("`") -> {
                    val inner = matchValue.substring(1, matchValue.length - 1)
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(inner)
                    }
                }
                matchValue.startsWith("*") || matchValue.startsWith("_") -> {
                    val inner = matchValue.substring(1, matchValue.length - 1)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(inner)
                    }
                }
            }
            lastIndex = end
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}
