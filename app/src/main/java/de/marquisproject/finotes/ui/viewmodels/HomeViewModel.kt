package de.marquisproject.finotes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.marquisproject.finotes.data.notes.model.Note
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
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategories = MutableStateFlow<List<Int>>(emptyList())
    private val _selectedNotes = MutableStateFlow<List<Note>>(emptyList())
    private val _inSelectionMode = _selectedNotes.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    private val _snackbarEventChannel = Channel<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventChannel.receiveAsFlow()
    private var lastAction: LastAction? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _pinnedNotesDisplay = combine(
        _searchQuery,
        _selectedCategories
    ) { query, categories ->
        query to categories
    }.flatMapLatest{
            (query, categories) ->
        noteRepository.getNotesWithQueryAndPinnedStatusAndCategory(
            searchQuery = query,
            isPinned = true,
            categoryQueryIds = categories,
            ignoreCategoryFilter = categories.isEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _normalNotesDisplay = combine(
        _searchQuery,
        _selectedCategories
    ) { query, categories ->
        query to categories
    }.flatMapLatest{
            (query, categories) ->
        noteRepository.getNotesWithQueryAndPinnedStatusAndCategory(
            searchQuery = query,
            isPinned = false,
            categoryQueryIds = categories,
            ignoreCategoryFilter = categories.isEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // public vals to expose the state of the UI to the UI layer (marked with val)
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedCategories: StateFlow<List<Int>> = _selectedCategories.asStateFlow()
    val inSelectionMode: StateFlow<Boolean> = _inSelectionMode // already a StateFlow
    val selectedNotes: StateFlow<List<Note>> = _selectedNotes.asStateFlow()
    val pinnedNotesDisplay = _pinnedNotesDisplay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val normalNotesDisplay = _normalNotesDisplay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Functions to update the state of the UI and perform repository operations which in turn interact with the database
    // These functions are called from the UI layer
    fun setQuery(query: String) {
        _searchQuery.update { query }
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
        _selectedNotes.update { emptyList() }
    }

    fun selectAllNotes() {
        _selectedNotes.update { _pinnedNotesDisplay.value + _normalNotesDisplay.value }
    }

    fun archiveSelectedNotes() {
        viewModelScope.launch {
            val notesToArchive = _selectedNotes.value.toList()
            val archivedNotes = mutableListOf<Note>()
            notesToArchive.forEach { note ->
                val newId = noteRepository.archiveNote(note) //TODO() make this a batch operation
                archivedNotes.add(note.copy(id = newId))
            }
            lastAction = LastAction.Archive(archivedNotes)
            _snackbarEventChannel.send(
                SnackbarEvent.ShowSnackbar(
                    message = "${archivedNotes.size} note(s) archived",
                    actionLabel = "Undo"
                )
            )
            clearSelection()
        }
    }

    fun binSelectedNotes() {
        viewModelScope.launch {
            val notesToBin = _selectedNotes.value.toList()
            val binnedNotes = mutableListOf<Note>()
            notesToBin.forEach { note ->
                val newId = noteRepository.binNote(note)
                binnedNotes.add(note.copy(id = newId))
            }
            lastAction = LastAction.Bin(binnedNotes)
            _snackbarEventChannel.send(
                SnackbarEvent.ShowSnackbar(
                    message = "${binnedNotes.size} note(s) moved to bin",
                    actionLabel = "Undo"
                )
            )
            clearSelection()
        }
    }

    fun pinSelectedNotes() {
        viewModelScope.launch {
            selectedNotes.value.forEach { note ->
                noteRepository.updateNote(note.copy(isPinned = true))
            }
            clearSelection()
        }
    }

    fun unpinSelectedNotes() {
        viewModelScope.launch {
            _selectedNotes.value.forEach { note ->
                noteRepository.updateNote(note.copy(isPinned = false))
            }
            clearSelection()
        }
    }

    fun performUndo() {
        viewModelScope.launch {
            when (val action = lastAction) {
                is LastAction.Archive -> {
                    val notesArchived = action.notes
                    notesArchived.forEach { note ->
                        noteRepository.unarchiveNote(note)
                    }
                    _snackbarEventChannel.send(
                        SnackbarEvent.ShowSnackbar(
                            message = "Archived note(s) restored",
                            actionLabel = null // No undo for undo
                        )
                    )
                }
                is LastAction.Bin -> {
                    val notesBinned = action.notes
                    notesBinned.forEach { note ->
                        noteRepository.restoreNote(note)
                    }
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
        /**
         * Represents an action to archive notes.
         * @property notes The list of notes that has been archived. with the ids in the archive after archiving
         */
        data class Archive(val notes: List<Note>) : LastAction()
        data class Bin(val notes: List<Note>) : LastAction()
        // Add other actions if needed
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getNotesDisplayFlow(isPinned: Boolean): StateFlow<List<Note>> {
        return combine(
            _searchQuery,
            _selectedCategories
        ) { query, categories ->
            query to categories
        }.flatMapLatest { (query, categories) ->
            noteRepository.getNotesWithQueryAndPinnedStatusAndCategory(
                searchQuery = query,
                isPinned = isPinned,
                categoryQueryIds = categories,
                ignoreCategoryFilter = categories.isEmpty()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

}