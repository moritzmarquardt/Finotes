package de.marquisproject.finotes.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.marquisproject.finotes.data.notes.repositories.DataStoreInstance
import de.marquisproject.finotes.ui.theme.ThemeVariant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _themeVariant = MutableStateFlow<ThemeVariant?>(null)
    val themeVariant: StateFlow<ThemeVariant?> = _themeVariant

    init {
        getThemeVariant()
    }

    private fun getThemeVariant() {
        viewModelScope.launch {
            DataStoreInstance.getThemeVariant(
                context = getApplication()
            ).collect { theme ->
                _themeVariant.update { theme }
            }
        }
    }

    fun saveThemeVariant(value: ThemeVariant) {
        viewModelScope.launch {
            DataStoreInstance.saveThemeVariant(
                context = getApplication(),
                value = value
            )
        }
    }


}