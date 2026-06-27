package com.shub39.grit.core.data.datastore

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.shub39.grit.core.interfaces.ThemeDatastore
import com.shub39.grit.core.theme.AppTheme
import com.shub39.grit.core.theme.Fonts
import com.shub39.grit.core.theme.PaletteStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [ThemeDatastore::class])
class ThemeDatastoreImpl(private val datastore: DataStore<Preferences>) : ThemeDatastore {
    companion object {
        private val appThemeKey = stringPreferencesKey("app_theme")
        private val seedColorKey = intPreferencesKey("seed_color")
        private val amoledKey = booleanPreferencesKey("amoled")
        private val paletteKey = stringPreferencesKey("palette")
        private val materialYouKey = booleanPreferencesKey("material_you")
        private val fontPrefKey = stringPreferencesKey("font")
        private val hapticPrefKey = booleanPreferencesKey("haptic")
    }

    override suspend fun resetAppTheme() {
        datastore.edit { settings ->
            settings[seedColorKey] = Color.White.toArgb()
            settings[amoledKey] = false
            settings[paletteKey] = PaletteStyle.TONALSPOT.name
            settings[materialYouKey] = false
            settings[fontPrefKey] = Fonts.FIGTREE.name
        }
    }

    override fun getAppThemeFlow(): Flow<AppTheme> =
        datastore.data.map { prefs ->
            val appTheme = prefs[appThemeKey] ?: AppTheme.SYSTEM.name
            AppTheme.valueOf(appTheme)
        }

    override suspend fun setAppTheme(theme: AppTheme) {
        datastore.edit { prefs -> prefs[appThemeKey] = theme.name }
    }

    override fun getSeedColorFlow(): Flow<Int> =
        datastore.data.map { prefs -> prefs[seedColorKey] ?: Color.White.toArgb() }

    override suspend fun setSeedColor(color: Int) {
        datastore.edit { prefs -> prefs[seedColorKey] = color }
    }

    override fun getAmoledPref(): Flow<Boolean> =
        datastore.data.map { prefs -> prefs[amoledKey] == true }

    override suspend fun setAmoledPref(pref: Boolean) {
        datastore.edit { prefs -> prefs[amoledKey] = pref }
    }

    override fun getPaletteStyle(): Flow<PaletteStyle> =
        datastore.data.map { prefs ->
            try {
                val style = prefs[paletteKey] ?: PaletteStyle.TONALSPOT.name
                return@map PaletteStyle.valueOf(style)
            } catch (_: Exception) {
                return@map PaletteStyle.TONALSPOT
            }
        }

    override suspend fun setPaletteStyle(style: PaletteStyle) {
        datastore.edit { prefs -> prefs[paletteKey] = style.name }
    }

    override fun getMaterialYouFlow(): Flow<Boolean> =
        datastore.data.map { prefs -> prefs[materialYouKey] == true }

    override suspend fun setMaterialYou(pref: Boolean) {
        datastore.edit { prefs -> prefs[materialYouKey] = pref }
    }

    override fun getFontPrefFlow(): Flow<Fonts> =
        datastore.data.map { prefs ->
            val font = prefs[fontPrefKey] ?: Fonts.FIGTREE.name
            Fonts.valueOf(font)
        }

    override suspend fun setFontPref(font: Fonts) {
        datastore.edit { prefs -> prefs[fontPrefKey] = font.name }
    }
}