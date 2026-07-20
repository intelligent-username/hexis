package com.loc.hexis.core.data

import android.content.Context
import android.util.Log
import com.loc.hexis.core.app.Changelog
import com.loc.hexis.core.interfaces.ChangelogManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single(binds = [ChangelogManager::class])
class ChangelogManagerImpl(private val context: Context) : ChangelogManager {
    private val _changelogs: MutableStateFlow<Changelog> = MutableStateFlow(emptyList())
    override val changelogs = _changelogs.asStateFlow().onStart { getChangelogs() }

    private fun getChangelogs() {
        try {
            val rawJson =
                context.assets.open("changelog.json").bufferedReader().use { it.readText() }

            val json = Json.decodeFromString<Changelog>(rawJson)

            _changelogs.update { json }
        } catch (e: Exception) {
            Log.e("ChangelogManager", "Error reading changelog", e)
        }
    }
}
