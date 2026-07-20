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
            trimmed.startsWith("> ") ->
                FormattedLine(LineType.QUOTE, trimmed.removePrefix("> "), indent / 2)
            numberedListRegex.matches(trimmed) -> {
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

fun getLinePrefix(line: FormattedLine): String {
    return when (line.type) {
        LineType.HEADER -> "# "
        LineType.SUB_HEADER -> "## "
        LineType.SUB_SUB_HEADER -> "### "
        LineType.BULLET_LIST -> "* "
        LineType.NUMBERED_LIST -> "${line.number ?: 1}. "
        LineType.CHECKLIST -> if (line.isChecked) "- [x] " else "- [ ] "
        LineType.QUOTE -> "> "
        LineType.HORIZONTAL_RULE -> "---\n"
        LineType.REGULAR -> ""
    }
}

fun isListLine(text: String): Boolean {
    val trimmed = text.trimStart()
    return trimmed.startsWith("* ") ||
        trimmed.startsWith("- ") ||
        checklistRegex.matches(trimmed) ||
        numberedListRegex.matches(trimmed)
}

fun getListPrefix(text: String): String? {
    val trimmed = text.trimStart()
    return when {
        checklistRegex.matches(trimmed) -> {
            val match = checklistRegex.find(trimmed)!!
            val isChecked = match.groupValues[2].equals("x", ignoreCase = true)
            if (isChecked) "- [x] " else "- [ ] "
        }
        trimmed.startsWith("* ") -> "* "
        trimmed.startsWith("- ") -> "- "
        numberedListRegex.matches(trimmed) -> {
            val match = numberedListRegex.find(trimmed)!!
            val num = match.groupValues[1]
            val sep = if (trimmed.contains(')')) ')' else '.'
            "$num$sep "
        }
        else -> null
    }
}

fun getNextListPrefix(text: String): String? {
    val trimmed = text.trimStart()
    return when {
        checklistRegex.matches(trimmed) -> "- [ ] "
        trimmed.startsWith("* ") -> "* "
        trimmed.startsWith("- ") -> "- "
        numberedListRegex.matches(trimmed) -> {
            val match = numberedListRegex.find(trimmed)!!
            val num = match.groupValues[1].toIntOrNull() ?: return null
            val sep = if (trimmed.contains(')')) ')' else '.'
            "${num + 1}$sep "
        }
        else -> null
    }
}

fun removePrefix(text: String): String {
    val trimmed = text.trimStart()
    return when {
        trimmed.startsWith("### ") -> trimmed.removePrefix("### ")
        trimmed.startsWith("## ") -> trimmed.removePrefix("## ")
        trimmed.startsWith("# ") -> trimmed.removePrefix("# ")
        checklistRegex.matches(trimmed) -> {
            val match = checklistRegex.find(trimmed)!!
            match.groupValues[3]
        }
        trimmed.startsWith("* ") -> trimmed.removePrefix("* ")
        trimmed.startsWith("- ") -> trimmed.removePrefix("- ")
        trimmed.startsWith("> ") -> trimmed.removePrefix("> ")
        numberedListRegex.matches(trimmed) -> trimmed.replaceFirst(numberedListRegex, "")
        trimmed.matches(Regex("""^[-*_]{3,}\s*$""")) -> ""
        else -> trimmed
    }
}

fun getContentPreview(content: String, maxChars: Int = 120): String {
    val lines = parseContentLines(content)
    val previewParts = lines.mapNotNull { line ->
        when (line.type) {
            LineType.HEADER, LineType.SUB_HEADER, LineType.SUB_SUB_HEADER -> line.text.ifEmpty { null }
            LineType.BULLET_LIST -> if (line.text.isNotEmpty()) "• ${line.text}" else null
            LineType.NUMBERED_LIST -> if (line.text.isNotEmpty()) "${line.number ?: 1}. ${line.text}" else null
            LineType.CHECKLIST -> if (line.text.isNotEmpty()) "${if (line.isChecked) "☑" else "☐"} ${line.text}" else null
            LineType.QUOTE -> if (line.text.isNotEmpty()) "│ ${line.text}" else null
            LineType.HORIZONTAL_RULE -> null
            LineType.REGULAR -> line.text.ifEmpty { null }
        }
    }
    return previewParts
        .joinToString("\n")
        .trim()
        .take(maxChars)
}
