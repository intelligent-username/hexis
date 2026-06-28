package com.loc.hexis.shared.ui.app

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.loc.hexis.core.app.VersionEntry
import com.loc.hexis.core.settings.Sections
import com.loc.hexis.core.theme.Theme

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