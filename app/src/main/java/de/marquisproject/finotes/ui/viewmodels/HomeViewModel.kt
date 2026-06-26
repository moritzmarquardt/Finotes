package de.marquisproject.finotes.ui.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.marquisproject.finotes.data.notes.model.Category
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.repositories.CategoryRepository
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val searchQuery = TextFieldState()
    private val _selectedCategories = MutableStateFlow<Set<Long>>(emptySet())
    private val _selectedNotes = MutableStateFlow<Set<Note>>(emptySet())
    private val _categoryFilterVisible = MutableStateFlow(false)
    private val _inSelectionMode = _selectedNotes.map {
        it.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT), false)
    private val _snackbarEventChannel = Channel<SnackbarEvent>()
    private var lastAction: LastAction? = null

    // public vals to expose the state of the UI to the UI layer (marked with val)
    val selectedCategories: StateFlow<Set<Long>> = _selectedCategories.asStateFlow()
    val categoryFilterVisible: StateFlow<Boolean> = _categoryFilterVisible.asStateFlow()
    val usedCategories: StateFlow<List<Category>> = categoryRepository.getUsedCategoriesSortedByRelevance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT), emptyList())
    val allCategories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT), emptyList())
    val selectedNotes: StateFlow<Set<Note>> = _selectedNotes.asStateFlow()
    val inSelectionMode: StateFlow<Boolean> = _inSelectionMode // already a StateFlow
    val snackbarEventFlow = _snackbarEventChannel.receiveAsFlow()

    val pinnedNotesDisplay: StateFlow<List<Note>> = getNotesDisplayFlow(isPinned = true)
    val normalNotesDisplay: StateFlow<List<Note>> = getNotesDisplayFlow(isPinned = false)

    // Functions to update the state of the UI and perform repository operations which in turn interact with the database
    // These functions are called from the UI layer
    fun setQuery(query: String) {
        searchQuery.setTextAndPlaceCursorAtEnd(query)
    }

    fun toggleCategoryFilter() {
        _categoryFilterVisible.update { !it }
    }

    fun toggleCategory(categoryId: Long) {
        val isCurrentlySelected = _selectedCategories.value.contains(categoryId)
        if (!isCurrentlySelected) {
            viewModelScope.launch {
                categoryRepository.logCategoryUsage(categoryId)
            }
        }
        _selectedCategories.update { current ->
            if (isCurrentlySelected) {
                current - categoryId
            } else {
                current + categoryId
            }
        }
    }

    fun longClickSelect(note: Note) {
        if (!_selectedNotes.value.contains(note)) {
            _selectedNotes.update { it + note }
        }
    }

    fun shortClickSelect(note: Note, shortClickAction: () -> Unit) {
        if (_inSelectionMode.value) {
            if (_selectedNotes.value.contains(note)) {
                _selectedNotes.update { it - note }
            } else {
                _selectedNotes.update { it + note }
            }
        } else {
            shortClickAction()
        }
    }

    fun clearSelection() {
        _selectedNotes.update { emptySet() }
    }

    /**
     * Toggles the selection of all pinned notes. SO if all are selected, they are deselected.
     * If none are selected, they are all selected.
     * @param add If true, adds all pinned notes to the selection. If false, removes all pinned notes from the selection.
     */
    fun toggleSelectAllPinnedNotes(add: Boolean = false) {
        if (_selectedNotes.value.containsAll(pinnedNotesDisplay.value)) {
            _selectedNotes.update { _selectedNotes.value - pinnedNotesDisplay.value.toSet() }
            return
        } else if (add) {
            _selectedNotes.update { it + pinnedNotesDisplay.value.toSet()}
        } else {
            _selectedNotes.update { pinnedNotesDisplay.value.toSet() }
        }
    }

    fun toggleSelectAllNonPinnedNotes(add: Boolean = false) {
        if (_selectedNotes.value.containsAll(normalNotesDisplay.value)) {
            _selectedNotes.update { _selectedNotes.value - normalNotesDisplay.value.toSet() }
            return
        } else if (add) {
            _selectedNotes.update { it + normalNotesDisplay.value.toSet() }
        } else {
            _selectedNotes.update { normalNotesDisplay.value.toSet() }
        }
    }

    /**
     * Checks if all pinned notes are selected.
     * @param exclusive If true, checks if the selected notes are exactly the pinned notes. If false, checks if all pinned notes are included in the selected notes.
     */
    fun allPinnedNotesSelected(exclusive: Boolean = false): Boolean {
        return if (exclusive) {
            _selectedNotes.value == pinnedNotesDisplay.value.toSet()
        } else {
            _selectedNotes.value.containsAll(pinnedNotesDisplay.value) && pinnedNotesDisplay.value.isNotEmpty()
        }
    }

    /**
     * Checks if all non-pinned notes are selected.
     * @param exclusive If true, checks if the selected notes are exactly the non-pinned notes. If false, checks if all non-pinned notes are included in the selected notes.
     */
    fun allNonPinnedNotesSelected(exclusive: Boolean = false): Boolean {
        return if (exclusive) {
            _selectedNotes.value == normalNotesDisplay.value.toSet()
        } else {
            _selectedNotes.value.containsAll(normalNotesDisplay.value) && normalNotesDisplay.value.isNotEmpty()
        }
    }

    fun archiveSelectedNotes() {
        viewModelScope.launch {
            val notesToArchive = _selectedNotes.value.toList()
            noteRepository.archiveNotes(notesToArchive)
            lastAction = LastAction.Archive(notesToArchive)
            _snackbarEventChannel.send(
                SnackbarEvent.ShowSnackbar(
                    message = "${notesToArchive.size} note(s) archived",
                    actionLabel = "Undo"
                )
            )
            clearSelection()
        }
    }

    fun binSelectedNotes() {
        viewModelScope.launch {
            val notesToBin = _selectedNotes.value.toList()
            noteRepository.binNotes(notesToBin)
            lastAction = LastAction.Bin(notesToBin)
            _snackbarEventChannel.send(
                SnackbarEvent.ShowSnackbar(
                    message = "${notesToBin.size} note(s) moved to bin",
                    actionLabel = "Undo"
                )
            )
            clearSelection()
        }
    }

    fun pinSelectedNotes() {
        viewModelScope.launch {
            val notesToPin: List<Note> = _selectedNotes.value.toList()
            noteRepository.updateNotes(notesToPin.map { it.copy(isPinned = true) })
            clearSelection()
        }
    }

    fun unpinSelectedNotes() {
        viewModelScope.launch {
            val notesToUnpin: List<Note> = _selectedNotes.value.toList()
            noteRepository.updateNotes(notesToUnpin.map { it.copy(isPinned = false) })
            clearSelection()
        }
    }

    fun updateSelectedNotesCategory(categoryId: Long) {
        viewModelScope.launch {
            val notesToUpdate = _selectedNotes.value.toList()
            noteRepository.updateNotes(notesToUpdate.map { it.copy(category = categoryId) })
            clearSelection()
        }
    }

    fun performUndo() {
        viewModelScope.launch {
            when (val action = lastAction) {
                is LastAction.Archive -> {
                    noteRepository.restoreNotes(action.notes)
                    _snackbarEventChannel.send(
                        SnackbarEvent.ShowSnackbar(
                            message = "Archived note(s) restored",
                            actionLabel = null // No undo for undo
                        )
                    )
                }
                is LastAction.Bin -> {
                    noteRepository.restoreNotes(action.notes)
                    _snackbarEventChannel.send(
                        SnackbarEvent.ShowSnackbar(
                            message = "Note(s) restored from bin",
                            actionLabel = null // No undo for undo
                        )
                    )
                }
                null -> {
                    // No action to undo
                    _snackbarEventChannel.send(
                        SnackbarEvent.ShowSnackbar(
                            message = "Error: No action to undo",
                            actionLabel = null
                        )
                    )
                }
            }
            lastAction = null // Clear the last action after undo
        }
    }


    // Sealed class to represent different types of Snackbar events
    sealed class SnackbarEvent {
        data class ShowSnackbar(val message: String, val actionLabel: String?) : SnackbarEvent()
    }

    // Sealed class to represent the last action performed for undo
    private sealed class LastAction {
        data class Archive(val notes: List<Note>) : LastAction()
        data class Bin(val notes: List<Note>) : LastAction()
        // Add other actions if needed
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getNotesDisplayFlow(isPinned: Boolean): StateFlow<List<Note>> {
        return combine(
            snapshotFlow { searchQuery.text },
            _selectedCategories
        ) { query, categories ->
            query.toString() to categories.toList()
        }.flatMapLatest { (query, categories) ->
            noteRepository.fetchNotesWithQuery(
                searchQuery = query,
                isPinned = isPinned,
                categories = categories.takeIf { it.isNotEmpty() },
                noteStatus = NoteStatus.ACTIVE
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT), emptyList())
    }

    companion object {
        private const val WHILE_SUBSCRIBED_TIMEOUT = 5000L
    }
}