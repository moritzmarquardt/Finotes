package de.marquisproject.fionotes.ui.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    // Expose screen UI state
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _allNotes = MutableStateFlow(emptyList<Note>())
    val allNotes: StateFlow<List<Note>> = _allNotes.asStateFlow()

    init {
        viewModelScope.launch {
            noteRepository.allNotes.flowOn(Dispatchers.IO).collect {
                _allNotes.value = it
            }
        }
    }

    //val allNotes: LiveData<List<Note>> = noteRepository.allNotes.asLiveData()

    fun insert(note: Note) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            noteRepository.insert(note)
        }
    }

    fun update(note: Note) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            noteRepository.update(note)
        }
    }

    fun delete(note: Note) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            noteRepository.delete(note)
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

}