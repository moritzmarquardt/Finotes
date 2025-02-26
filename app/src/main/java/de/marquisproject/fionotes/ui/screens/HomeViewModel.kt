package de.marquisproject.fionotes.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import de.marquisproject.fionotes.data.notes.repositories.NoteRepository
import de.marquisproject.fionotes.data.notes.sources.NoteDatabase
import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.ui.screens.HomeUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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