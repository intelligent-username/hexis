package com.shub39.grit.shared.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import com.shub39.grit.core.theme.Theme
import com.shub39.grit.shared.ui.theme.HexisTheme

class HexisPreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable (() -> Unit)) {
        HexisTheme(theme = Theme()) { Surface(content = content) }
    }
}