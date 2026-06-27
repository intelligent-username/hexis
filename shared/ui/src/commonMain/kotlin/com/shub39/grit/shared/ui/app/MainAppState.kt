package com.shub39.grit.shared.ui.app

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.shub39.grit.core.app.VersionEntry
import com.shub39.grit.core.settings.Sections
import com.shub39.grit.core.theme.Theme

@Stable
@Immutable
data class MainAppState(
    val isAppUnlocked: Boolean = false,
    val isBiometricLockOn: Boolean? = null,
    val startingSection: Sections = Sections.Tasks,
    val theme: Theme = Theme(),
    val currentChangelog: VersionEntry? = null,
    val shortcutAction: String? = null,
)