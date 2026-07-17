package com.loc.hexis.shared.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import com.loc.hexis.core.theme.Theme
import com.loc.hexis.shared.ui.theme.HexisTheme

class HexisPreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable (() -> Unit)) {
        HexisTheme(theme = Theme()) { Surface(content = content) }
    }
}
