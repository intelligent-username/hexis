package com.loc.hexis.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.loc.hexis.core.theme.AppTheme.DARK
import com.loc.hexis.core.theme.AppTheme.LIGHT
import com.loc.hexis.core.theme.AppTheme.SYSTEM
import com.loc.hexis.core.theme.Theme
import com.loc.hexis.shared.ui.toFontRes
import com.loc.hexis.shared.ui.toMPaletteStyle
import com.materialkolor.rememberDynamicColorScheme

@Composable
actual fun HexisTheme(theme: Theme, content: @Composable (() -> Unit)) {
    val isDark =
        when (theme.appTheme) {
            SYSTEM -> isSystemInDarkTheme()
            DARK -> true
            LIGHT -> false
        }

    val colorScheme =
        rememberDynamicColorScheme(
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
