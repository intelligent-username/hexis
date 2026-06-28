package com.loc.hexis.core.theme

/** data class representing the current theme of the app */
data class Theme(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val isAmoled: Boolean = false,
    val isMaterialYou: Boolean = false,
    val font: Fonts = Fonts.FIGTREE,
    val paletteStyle: PaletteStyle = PaletteStyle.TONALSPOT,
    val seedColor: Int = 0xFFFFFF,
)