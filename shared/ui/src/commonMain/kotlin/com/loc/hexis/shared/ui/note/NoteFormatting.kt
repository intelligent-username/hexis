package com.loc.hexis.shared.ui.note

enum class LineType {
    HEADER,
    SUB_HEADER,
    SUB_SUB_HEADER,
    BULLET_LIST,
    NUMBERED_LIST,
    REGULAR,
}

data class FormattedLine(
    val type: LineType,
    val text: String,
    val indent: Int = 0,
    val number: Int? = null,
)

private val numberedListRegex = Regex("""^(\d+)[\.\)]\s""")

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
            trimmed.startsWith("* ") ->
                FormattedLine(LineType.BULLET_LIST, trimmed.removePrefix("* "), indent / 2)
            trimmed.startsWith("- ") ->
                FormattedLine(LineType.BULLET_LIST, trimmed.removePrefix("- "), indent / 2)
            numberedListRegex.matches(trimmed) -> {
                val match = numberedListRegex.find(trimmed)!!
                val num = match.groupValues[1].toIntOrNull()
                val rest = trimmed.replaceFirst(numberedListRegex, "")
                FormattedLine(LineType.NUMBERED_LIST, rest, indent / 2, num)
            }
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
        LineType.REGULAR -> ""
    }
}

fun isListLine(text: String): Boolean {
    val trimmed = text.trimStart()
    return trimmed.startsWith("* ") ||
        trimmed.startsWith("- ") ||
        numberedListRegex.matches(trimmed)
}

fun getListPrefix(text: String): String? {
    val trimmed = text.trimStart()
    return when {
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
        trimmed.startsWith("* ") -> trimmed.removePrefix("* ")
        trimmed.startsWith("- ") -> trimmed.removePrefix("- ")
        numberedListRegex.matches(trimmed) -> trimmed.replaceFirst(numberedListRegex, "")
        else -> trimmed
    }
}

fun getContentPreview(content: String, maxChars: Int = 120): String {
    return parseContentLines(content)
        .joinToString(" ") { it.text }
        .replace("\n", " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(maxChars)
}
