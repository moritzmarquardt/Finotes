package de.marquisproject.finotes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    // private vals to hold the state of the UI internally (marked with _)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedNotes = MutableStateFlow<List<Note>>(emptyList())
    private val _inSelectionMode = _selectedNotes.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    private val _notesList = _searchQuery
        .flatMapLatest { searchQuery ->
            noteRepository.fetchNotesWithQuery(
                searchQuery = searchQuery.takeIf { it.isNotBlank() },
                noteStatus = NoteStatus.ARCHIVED,
                isPinned = null,
                categories = null
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    // public vals to expose the state of the UI to the UI layer (marked with val)
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val notesList: StateFlow<List<Note>> = _notesList // already a StateFlow
    val inSelectionMode: StateFlow<Boolean> = _inSelectionMode // already a StateFlow
    val selectedNotes: StateFlow<List<Note>> = _selectedNotes.asStateFlow()

    // Functions to update the state of the UI and perform repository operations which in turn interact with the database
    // These functions are called from the UI layer

    fun setQuery(query: String) {
        _searchQuery.update { query }
    }

    fun longClickSelect(note: Note) {
        /*if (!_inSelectionMode.value) {
            _uiState.value = _uiState.value.copy(inSelectionMode = true)
        }*/
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

    fun unarchiveSelectedNotes() {
        viewModelScope.launch {
            _selectedNotes.value.forEach { note ->
                noteRepository.restoreNote(note)
            }
            clearSelection()
        }
    }

    fun binSelectedNotes() {
        viewModelScope.launch {
            _selectedNotes.value.forEach { note ->
                noteRepository.binNote(note)
            }
            clearSelection()
        }
    }
}