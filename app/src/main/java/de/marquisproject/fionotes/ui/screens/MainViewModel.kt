package de.marquisproject.fionotes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    // Expose screen UI state
    private val _uiState = MutableStateFlow(MainUiState())
    private val _searchQuery = MutableStateFlow("")
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _notesList = _searchQuery
        .flatMapLatest { searchQuery ->
            if (searchQuery.isBlank()) {
                noteRepository.getAllNotes()
            } else {
                noteRepository.searchNotes(searchQuery)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val uiState = combine(_uiState, _notesList, _searchQuery) {
        uiState, notesList, searchQuery ->
        uiState.copy(
            notesList = notesList,
            searchQuery = searchQuery)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState())


    fun upsertNote(note: Note) {
        //viewmodel scope because we use suspend functions
        viewModelScope.launch {
            noteRepository.upsertNote(note)
        }
    }

    fun deleteNote(note: Note) {
        //viewmodel scope because we use suspend functions
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

}