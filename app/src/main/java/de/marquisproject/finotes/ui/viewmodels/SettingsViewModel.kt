package de.marquisproject.finotes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.marquisproject.finotes.data.settings.SettingsRepository
import de.marquisproject.finotes.data.notes.model.Category
import de.marquisproject.finotes.data.notes.repositories.CategoryRepository
import de.marquisproject.finotes.ui.theme.ThemeVariant
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val themeVariant: StateFlow<ThemeVariant?> = settingsRepository.themeVariant
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    private val _snackbarEventChannel = Channel<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventChannel.receiveAsFlow()

    private var lastDeletedCategory: Category? = null

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveThemeVariant(value: ThemeVariant) {
        viewModelScope.launch {
            settingsRepository.saveThemeVariant(value)
        }
    }

    fun addCategory(name: String, color: Int) {
        viewModelScope.launch {
            categoryRepository.insertCategory(Category(name = name, color = color))
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            lastDeletedCategory = category
            categoryRepository.deleteCategory(category)
            _snackbarEventChannel.send(
                SnackbarEvent.ShowSnackbar(
                    message = "Category '${category.name}' deleted",
                    actionLabel = "Undo"
                )
            )
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            lastDeletedCategory?.let {
                categoryRepository.insertCategory(it)
                lastDeletedCategory = null
            }
        }
    }

    sealed class SnackbarEvent {
        data class ShowSnackbar(val message: String, val actionLabel: String?) : SnackbarEvent()
    }
}
