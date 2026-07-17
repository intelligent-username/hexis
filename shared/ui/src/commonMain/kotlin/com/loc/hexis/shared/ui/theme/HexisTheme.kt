package com.loc.hexis.shared.ui.theme

import androidx.compose.runtime.Composable
import com.loc.hexis.core.theme.Theme

/** Wrapper to apply theme from [com.loc.hexis.core.theme.Theme] to all composables */
@Composable expect fun HexisTheme(theme: Theme = Theme(), content: @Composable () -> Unit)
