package com.shub39.grit.shared.ui.theme

import androidx.compose.runtime.Composable
import com.shub39.grit.core.theme.Theme

/** Wrapper to apply theme from [com.shub39.grit.core.theme.Theme] to all composables */
@Composable expect fun HexisTheme(theme: Theme = Theme(), content: @Composable () -> Unit)