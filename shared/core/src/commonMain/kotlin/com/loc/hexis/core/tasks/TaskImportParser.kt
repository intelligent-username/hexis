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

package com.loc.hexis.core.tasks

data class ParsedTaskItem(
    val title: String,
    val description: String = "",
)

object TaskImportParser {
    fun parse(text: String): List<ParsedTaskItem> {
        val lines = text.lines()
        val items = mutableListOf<ParsedTaskItem>()
        var i = 0
        while (i < lines.size) {
            val trimmedLine = lines[i].trim()
            if (trimmedLine.startsWith("-") || trimmedLine.startsWith("*")) {
                val title = trimmedLine.drop(1).trim()
                if (title.isNotEmpty()) {
                    val descLines = mutableListOf<String>()
                    var j = i + 1
                    while (j < lines.size) {
                        val nextTrimmed = lines[j].trim()
                        if (nextTrimmed.isBlank()) {
                            var k = j + 1
                            while (k < lines.size && lines[k].isBlank()) {
                                k++
                            }
                            if (k < lines.size && lines[k].trim().startsWith(">")) {
                                j = k
                                continue
                            } else {
                                break
                            }
                        } else if (nextTrimmed.startsWith(">")) {
                            val descContent = nextTrimmed.trimStart('>', ' ').trim()
                            if (descContent.isNotEmpty()) {
                                descLines.add(descContent)
                            }
                            j++
                        } else {
                            break
                        }
                    }
                    items.add(
                        ParsedTaskItem(
                            title = title,
                            description = descLines.joinToString("\n"),
                        )
                    )
                }
            }
            i++
        }
        return items
    }
}
