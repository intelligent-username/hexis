package com.shub39.grit.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import com.materialkolor.rememberDynamicColorScheme
import com.shub39.grit.core.theme.AppTheme.DARK
import com.shub39.grit.core.theme.AppTheme.LIGHT
import com.shub39.grit.core.theme.AppTheme.SYSTEM
import com.shub39.grit.core.theme.Theme
import com.shub39.grit.shared.ui.toFontRes
import com.shub39.grit.shared.ui.toMPaletteStyle

@Composable
actual fun HexisTheme(theme: Theme, content: @Composable (() -> Unit)) {
    val isDark = when (theme.appTheme) {
        SYSTEM -> isSystemInDarkTheme()
        DARK -> true
        LIGHT -> false
    }

    val colorScheme = rememberDynamicColorScheme(
        seedColor = Color(theme.seedColor),
        isDark = isDark,
        isAmoled = theme.isAmoled,
        style = theme.paletteStyle.toMPaletteStyle(),
    )

    MaterialExpressiveTheme(
        colorScheme = colorScheme.animated(),
        motionScheme = MotionScheme.expressive(),
        typography = provideTypography(theme.font.toFontRes()),
        content = content,
    )
}