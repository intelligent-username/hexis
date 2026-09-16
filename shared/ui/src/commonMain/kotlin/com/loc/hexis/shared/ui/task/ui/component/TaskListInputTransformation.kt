
package com.loc.hexis.shared.ui.task.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.text.TextRange

private val numberedDotRegex = Regex("""^(\d+)\.(?:\s+|$)""")
private val numberedBracketRegex = Regex("""^(\d+)\)(?:\s+|$)""")

object TaskListInputTransformation : InputTransformation {
    @OptIn(ExperimentalFoundationApi::class)
    override fun TextFieldBuffer.transformInput() {
        if (changes.changeCount == 0) return

        val currentText = asCharSequence()
        var newlineIndex = -1

        // Find newly inserted newline across all changes in the batch
        for (i in 0 until changes.changeCount) {
            val range = changes.getRange(i)
            for (idx in range.start until range.end) {
                if (idx < currentText.length && currentText[idx] == '\n') {
                    // Check if it's a keypress/enter (small change), not a multi-line paste
                    if (range.length <= 2) {
                        newlineIndex = idx
                        break
                    }
                }
            }
            if (newlineIndex != -1) break
        }

        if (newlineIndex == -1) return

        val textBeforeNewline = currentText.subSequence(0, newlineIndex).toString().removeSuffix("\r")
        val lineStart = textBeforeNewline.lastIndexOf('\n') + 1
        val prevLine = textBeforeNewline.substring(lineStart)

        val indent = prevLine.takeWhile { it == ' ' || it == '\t' }
        val trimmed = prevLine.substring(indent.length)

        when {
            trimmed.startsWith("* ") || trimmed == "*" -> {
                val content = if (trimmed == "*") "" else trimmed.removePrefix("* ").trim()
                if (content.isEmpty()) {
                    replace(lineStart + indent.length, newlineIndex, "")
                    selection = TextRange(lineStart + indent.length)
                } else {
                    val prefix = "$indent* "
                    replace(newlineIndex + 1, newlineIndex + 1, prefix)
                    selection = TextRange(newlineIndex + 1 + prefix.length)
                }
            }
            trimmed.startsWith("- ") || trimmed == "-" -> {
                val content = if (trimmed == "-") "" else trimmed.removePrefix("- ").trim()
                if (content.isEmpty()) {
                    replace(lineStart + indent.length, newlineIndex, "")
                    selection = TextRange(lineStart + indent.length)
                } else {
                    val prefix = "$indent- "
                    replace(newlineIndex + 1, newlineIndex + 1, prefix)
                    selection = TextRange(newlineIndex + 1 + prefix.length)
                }
            }
            numberedDotRegex.containsMatchIn(trimmed) -> {
                val match = numberedDotRegex.find(trimmed)!!
                val num = match.groupValues[1].toIntOrNull() ?: 1
                val content = trimmed.substring(match.range.last + 1).trim()
                if (content.isEmpty()) {
                    replace(lineStart + indent.length, newlineIndex, "")
                    selection = TextRange(lineStart + indent.length)
                } else {
                    val prefix = "$indent${num + 1}. "
                    replace(newlineIndex + 1, newlineIndex + 1, prefix)
                    selection = TextRange(newlineIndex + 1 + prefix.length)
                }
            }
            numberedBracketRegex.containsMatchIn(trimmed) -> {
                val match = numberedBracketRegex.find(trimmed)!!
                val num = match.groupValues[1].toIntOrNull() ?: 1
                val content = trimmed.substring(match.range.last + 1).trim()
                if (content.isEmpty()) {
                    replace(lineStart + indent.length, newlineIndex, "")
                    selection = TextRange(lineStart + indent.length)
                } else {
                    val prefix = "$indent${num + 1}) "
                    replace(newlineIndex + 1, newlineIndex + 1, prefix)
                    selection = TextRange(newlineIndex + 1 + prefix.length)
                }
            }
        }
    }
}
