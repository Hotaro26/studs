package com.example.studs.data.repository

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class ColorSchemeType { DEFAULT, MONOCHROME, DRACULA, MOCHA }

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("studs_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _colorScheme = MutableStateFlow(
        ColorSchemeType.valueOf(prefs.getString("color_scheme", ColorSchemeType.DEFAULT.name) ?: ColorSchemeType.DEFAULT.name)
    )
    val colorScheme: StateFlow<ColorSchemeType> = _colorScheme.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString("theme_mode", mode.name) }
        _themeMode.value = mode
    }

    fun setColorScheme(scheme: ColorSchemeType) {
        prefs.edit { putString("color_scheme", scheme.name) }
        _colorScheme.value = scheme
    }
}
