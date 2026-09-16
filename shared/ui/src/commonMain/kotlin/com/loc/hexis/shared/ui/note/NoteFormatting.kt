
package com.loc.hexis.shared.ui.note

enum class LineType {
    HEADER,
    SUB_HEADER,
    SUB_SUB_HEADER,
    BULLET_LIST,
    NUMBERED_LIST,
    CHECKLIST,
    QUOTE,
    HORIZONTAL_RULE,
    REGULAR,
}

data class FormattedLine(
    val type: LineType,
    val text: String,
    val indent: Int = 0,
    val number: Int? = null,
    val isChecked: Boolean = false,
)

private val numberedListRegex = Regex("""^(\d+)[\.\)]\s""")
private val checklistRegex = Regex("""^(-\s+|\*\s+)?\[([ xX])\]\s+(.*)""")

fun parseContentLines(content: String): List<FormattedLine> {
    return content.lines().map { line ->
        val trimmed = line.trimStart()
        val indent = line.length - trimmed.length

        when {
            trimmed.startsWith("### ") ->
                FormattedLine(LineType.SUB_SUB_HEADER, trimmed.removePrefix("### "), indent / 2)
            trimmed.startsWith("## ") ->
                FormattedLine(LineType.SUB_HEADER, trimmed.removePrefix("## "), indent / 2)
            trimmed.startsWith("# ") ->
                FormattedLine(LineType.HEADER, trimmed.removePrefix("# "), indent / 2)
            checklistRegex.matches(trimmed) -> {
                val match = checklistRegex.find(trimmed)!!
                val isChecked = match.groupValues[2].equals("x", ignoreCase = true)
                val itemText = match.groupValues[3]
                FormattedLine(LineType.CHECKLIST, itemText, indent / 2, isChecked = isChecked)
            }
            trimmed.startsWith("* ") ->
                FormattedLine(LineType.BULLET_LIST, trimmed.removePrefix("* "), indent / 2)
            trimmed.startsWith("- ") ->
                FormattedLine(LineType.BULLET_LIST, trimmed.removePrefix("- "), indent / 2)
            trimmed.startsWith("+ ") ->
                FormattedLine(LineType.BULLET_LIST, trimmed.removePrefix("+ "), indent / 2)
            trimmed.startsWith("> ") ->
                FormattedLine(LineType.QUOTE, trimmed.removePrefix("> "), indent / 2)
            numberedListRegex.containsMatchIn(trimmed) -> {
                val match = numberedListRegex.find(trimmed)!!
                val num = match.groupValues[1].toIntOrNull()
                val rest = trimmed.replaceFirst(numberedListRegex, "")
                FormattedLine(LineType.NUMBERED_LIST, rest, indent / 2, num)
            }
            trimmed.matches(Regex("""^[-*_]{3,}\s*$""")) ->
                FormattedLine(LineType.HORIZONTAL_RULE, trimmed.trimEnd(), indent / 2)
            else -> FormattedLine(LineType.REGULAR, line)
        }
    }
}


fun getNextListPrefix(text: String): String? {
    val indent = text.takeWhile { it == ' ' || it == '\t' }
    val trimmed = text.substring(indent.length)
    return when {
        checklistRegex.matches(trimmed) -> "$indent- [ ] "
        trimmed.startsWith("* ") || trimmed == "*" -> "$indent* "
        trimmed.startsWith("- ") || trimmed == "-" -> "$indent- "
        trimmed.startsWith("+ ") || trimmed == "+" -> "$indent+ "
        numberedListRegex.containsMatchIn(trimmed) -> {
            val match = numberedListRegex.find(trimmed)!!
            val num = match.groupValues[1].toIntOrNull() ?: return null
            val sep = if (match.value.contains(')')) ')' else '.'
            "$indent${num + 1}$sep "
        }
        else -> null
    }
}

fun removePrefix(text: String): String {
    val indent = text.takeWhile { it == ' ' || it == '\t' }
    val trimmed = text.substring(indent.length)
    return when {
        trimmed.startsWith("### ") -> trimmed.removePrefix("### ")
        trimmed.startsWith("## ") -> trimmed.removePrefix("## ")
        trimmed.startsWith("# ") -> trimmed.removePrefix("# ")
        checklistRegex.matches(trimmed) -> {
            val match = checklistRegex.find(trimmed)!!
            match.groupValues[3]
        }
        trimmed.startsWith("* ") -> trimmed.removePrefix("* ")
        trimmed == "*" -> ""
        trimmed.startsWith("- ") -> trimmed.removePrefix("- ")
        trimmed == "-" -> ""
        trimmed.startsWith("+ ") -> trimmed.removePrefix("+ ")
        trimmed == "+" -> ""
        trimmed.startsWith("> ") -> trimmed.removePrefix("> ")
        numberedListRegex.containsMatchIn(trimmed) -> trimmed.replaceFirst(numberedListRegex, "")
        trimmed.matches(Regex("""^[-*_]{3,}\s*$""")) -> ""
        else -> trimmed
    }
}

fun getContentPreview(content: String, maxChars: Int = 120): String {
    val lines = parseContentLines(content.trimEnd())
    val previewParts =
        lines.mapNotNull { line ->
            val text = line.text.trimEnd()
            when (line.type) {
                LineType.HEADER,
                LineType.SUB_HEADER,
                LineType.SUB_SUB_HEADER -> text.ifEmpty { null }
                LineType.BULLET_LIST -> if (text.isNotEmpty()) "• $text" else null
                LineType.NUMBERED_LIST ->
                    if (text.isNotEmpty()) "${line.number ?: 1}. $text" else null
                LineType.CHECKLIST ->
                    if (text.isNotEmpty()) "${if (line.isChecked) "☑" else "☐"} $text"
                    else null
                LineType.QUOTE -> if (text.isNotEmpty()) "│ $text" else null
                LineType.HORIZONTAL_RULE -> null
                LineType.REGULAR -> text.ifEmpty { null }
            }
        }
    return previewParts.joinToString("\n").trimEnd().take(maxChars)
}
