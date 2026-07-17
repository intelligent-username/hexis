package com.loc.hexis.shared.ui.app

import androidx.compose.runtime.Composable

@Composable expect fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit)
