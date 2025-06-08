package de.marquisproject.finotes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
            _selectedNotes.value.forEach { note ->
                noteRepository.archiveNote(note)
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

}