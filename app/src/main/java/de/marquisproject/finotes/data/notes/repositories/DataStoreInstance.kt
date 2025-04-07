package de.marquisproject.finotes.data.notes.repositories

import android.content.Context
import androidx.compose.ui.input.key.Key
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.marquisproject.finotes.ui.theme.ThemeVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreInstance {
    /**
     * This DataStore is used to store the settings of the app.
     * DataStore preferences is the recommended way to store key-value pairs in Android.
     */
    private val Context.dataStore: DataStore<Preferences>
            by preferencesDataStore(name = "settings")

    val THEME_KEY = stringPreferencesKey("theme_key")


    suspend fun saveThemeVariant(context: Context, value: ThemeVariant) {
        context.dataStore.edit { settings ->
            settings[THEME_KEY] = value.name
        }
    }

    fun getThemeVariant(context: Context): Flow<ThemeVariant?> {
        return context.dataStore.data.map { settings ->
            val theme = settings[THEME_KEY]
            if (theme == null) {
                ThemeVariant.FIONA // default theme
            } else {
                ThemeVariant.valueOf(theme)
            }
        }
    }

}