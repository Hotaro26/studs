package com.example.studs.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.studs.data.repository.ColorSchemeType
import com.example.studs.data.repository.SettingsRepository
import com.example.studs.data.repository.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = repository.themeMode
    val colorScheme: StateFlow<ColorSchemeType> = repository.colorScheme

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }

    fun setColorScheme(scheme: ColorSchemeType) {
        repository.setColorScheme(scheme)
    }

    companion object {
        fun provideFactory(repository: SettingsRepository): ViewModelProvider.Factory = 
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(repository) as T
                }
            }
    }
}
