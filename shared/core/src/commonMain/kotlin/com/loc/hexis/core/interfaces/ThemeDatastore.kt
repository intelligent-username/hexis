package com.loc.hexis.core.interfaces

import com.loc.hexis.core.theme.AppTheme
import com.loc.hexis.core.theme.Fonts
import com.loc.hexis.core.theme.PaletteStyle
import kotlinx.coroutines.flow.Flow

interface ThemeDatastore {
    suspend fun resetAppTheme()

    fun getAppThemeFlow(): Flow<AppTheme>

    suspend fun setAppTheme(theme: AppTheme)

    fun getSeedColorFlow(): Flow<Int>

    suspend fun setSeedColor(color: Int)

    fun getAmoledPref(): Flow<Boolean>

    suspend fun setAmoledPref(pref: Boolean)

    fun getPaletteStyle(): Flow<PaletteStyle>

    suspend fun setPaletteStyle(style: PaletteStyle)

    fun getMaterialYouFlow(): Flow<Boolean>

    suspend fun setMaterialYou(pref: Boolean)

    fun getFontPrefFlow(): Flow<Fonts>

    suspend fun setFontPref(font: Fonts)
}
