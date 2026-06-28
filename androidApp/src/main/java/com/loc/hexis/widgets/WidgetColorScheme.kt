package com.loc.hexis.widgets

import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.LocalContext
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders as Material3ColorProviders
import com.materialkolor.dynamicColorScheme
import com.loc.hexis.core.theme.AppTheme
import com.loc.hexis.core.theme.PaletteStyle
import com.loc.hexis.shared.ui.toMPaletteStyle

/**
 * Builds [ColorProviders] that mirror the app's user-selected theme so that
 * every [androidx.glance.GlanceTheme] call inside a widget reflects the same
 * color palette the user configured in Settings.
 *
 * The logic follows [com.loc.hexis.shared.ui.theme.HexisTheme] (Android
 * target) exactly:
 * - If Material You is enabled on Android 12+, use the system dynamic scheme.
 * - Otherwise derive a dynamic scheme from the stored seed color + palette style.
 * - Respect the light/dark/system preference for both light and dark slots.
 */
@Composable
fun rememberWidgetColorProviders(
    appTheme: AppTheme,
    seedColor: Int,
    isAmoled: Boolean,
    paletteStyle: PaletteStyle,
    isMaterialYou: Boolean,
): ColorProviders {
    val context = LocalContext.current

    val lightScheme =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isMaterialYou ->
                try {
                    dynamicLightColorScheme(context)
                } catch (e: Exception) {
                    dynamicColorScheme(
                        seedColor = Color(seedColor),
                        isDark = false,
                        isAmoled = false,
                        style = paletteStyle.toMPaletteStyle(),
                    )
                }

            else ->
                dynamicColorScheme(
                    seedColor = Color(seedColor),
                    isDark = false,
                    isAmoled = false,
                    style = paletteStyle.toMPaletteStyle(),
                )
        }

    val darkScheme =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isMaterialYou ->
                try {
                    dynamicDarkColorScheme(context)
                } catch (e: Exception) {
                    dynamicColorScheme(
                        seedColor = Color(seedColor),
                        isDark = true,
                        isAmoled = isAmoled,
                        style = paletteStyle.toMPaletteStyle(),
                    )
                }

            else ->
                dynamicColorScheme(
                    seedColor = Color(seedColor),
                    isDark = true,
                    isAmoled = isAmoled,
                    style = paletteStyle.toMPaletteStyle(),
                )
        }

    return Material3ColorProviders(light = lightScheme, dark = darkScheme)
}