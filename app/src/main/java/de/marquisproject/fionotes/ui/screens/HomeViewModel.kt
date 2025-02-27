package de.marquisproject.fionotes.ui.screens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    // Expose screen UI state
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /*val noteDB =
    val noteRepository = NoteRepository(noteDB)
    val notes: Flow<List<Note>> = noteRepository.getNotes()
    fun addNote(note: Note) {
        viewModelScope.launch {
            noteRepository.insertNote(note)
        }
    }*/

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

}