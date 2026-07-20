package com.loc.hexis.shared.ui.app

import androidx.compose.runtime.Composable

@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
    println("WARNING: SystemBackHandler not supported on WASM")
}
