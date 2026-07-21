package com.loc.hexis.shared.ui.note

import androidx.compose.ui.graphics.Color

data class NoteColorPreset(
    val id: String,
    val name: String,
    val lightHex: String,
    val darkHex: String
)

val noteColorPresets = listOf(
    NoteColorPreset("red", "Red", "#FFEF9A9A", "#FFC62828"),
    NoteColorPreset("orange", "Orange", "#FFFFE082", "#FFF57F17"),
    NoteColorPreset("yellow", "Yellow", "#FFFFF59D", "#FFFBC02D"),
    NoteColorPreset("green", "Green", "#FFC5E1A5", "#FF2E7D32"),
    NoteColorPreset("teal", "Teal", "#FF80CBC4", "#FF00695C"),
    NoteColorPreset("sky", "Sky", "#FF90CAF9", "#FF1565C0"),
    NoteColorPreset("blue", "Blue", "#FF9FA8DA", "#FF283593"),
    NoteColorPreset("purple", "Purple", "#FFCE93D8", "#FF6A1B9A"),
    NoteColorPreset("pink", "Pink", "#FFF48FB1", "#FFAD1457"),
    NoteColorPreset("brown", "Brown", "#FFBCAAA4", "#FF4E342E")
)

fun getNoteColor(colorHex: String?, isDark: Boolean): Color {
    if (colorHex.isNullOrBlank()) return Color.Unspecified
    if (colorHex.startsWith("preset:")) {
        val presetId = colorHex.removePrefix("preset:")
        val preset = noteColorPresets.firstOrNull { it.id == presetId } ?: return Color.Unspecified
        return parseColor(if (isDark) preset.darkHex else preset.lightHex)
    }
    return parseColor(colorHex)
}

fun parseColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    return try {
        when (clean.length) {
            6 -> Color((0xFF000000 or clean.toLong(16)).toLong())
            8 -> Color(clean.toLong(16))
            else -> Color.Unspecified
        }
    } catch (e: Exception) {
        Color.Unspecified
    }
}

fun Color.toHex(): String {
    val a = (alpha * 255).toInt().toString(16).padStart(2, '0')
    val r = (red * 255).toInt().toString(16).padStart(2, '0')
    val g = (green * 255).toInt().toString(16).padStart(2, '0')
    val b = (blue * 255).toInt().toString(16).padStart(2, '0')
    return "#$a$r$g$b"
}

