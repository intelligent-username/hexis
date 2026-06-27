package com.shub39.grit.shared.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/** CompositionLocal for window size class to make adaptive screens */
val LocalWindowSizeClass: ProvidableCompositionLocal<WindowSizeClass> = staticCompositionLocalOf {
    error("No window size class provided")
}