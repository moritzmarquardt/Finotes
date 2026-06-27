package de.marquisproject.finotes.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.marquisproject.finotes.ui.theme.ThemeVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val THEME_KEY = stringPreferencesKey("theme_key")
    }

    val themeVariant: Flow<ThemeVariant?> = dataStore.data.map { settings ->
        val theme = settings[THEME_KEY]
        if (theme == null) {
            ThemeVariant.AU // default theme
        } else {
            try {
                ThemeVariant.valueOf(theme)
            } catch (_: IllegalArgumentException) {
                ThemeVariant.AU
            }
        }
    }

    suspend fun saveThemeVariant(value: ThemeVariant) {
        dataStore.edit { settings ->
            settings[THEME_KEY] = value.name
        }
    }
}
