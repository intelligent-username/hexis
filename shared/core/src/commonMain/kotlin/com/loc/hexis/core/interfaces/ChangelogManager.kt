package com.loc.hexis.core.interfaces

import com.loc.hexis.core.app.Changelog
import kotlinx.coroutines.flow.Flow

interface ChangelogManager {
    val changelogs: Flow<Changelog>
}
