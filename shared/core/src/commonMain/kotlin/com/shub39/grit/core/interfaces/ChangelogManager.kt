package com.shub39.grit.core.interfaces

import com.shub39.grit.core.app.Changelog
import kotlinx.coroutines.flow.Flow

interface ChangelogManager {
    val changelogs: Flow<Changelog>
}