package de.marquisproject.fionotes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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

    private val _archivedList = noteRepository.getAllArchivedNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    private val _binList = noteRepository.getAllDeletedNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val uiState = combine(_uiState, _notesList, _searchQuery, _archivedList, _binList) {
        uiState, notesList, searchQuery, archivedList, binList ->
        uiState.copy(
            notesList = notesList,
            searchQuery = searchQuery,
            archivedList = archivedList,
            binList = binList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState())


    fun upsertNote(note: Note) {
        //viewmodel scope because we use suspend functions
        viewModelScope.launch {
            noteRepository.upsertNote(note)
        }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch {
            noteRepository.archiveNote(note)
        }
    }

    fun insertNewEmptyNote() {
       viewModelScope.launch {
           val emptyNote = Note()
           val newId = noteRepository.insertNote(emptyNote)
           setCurrentNote(emptyNote.copy(id = newId))
       }
    }

    fun deleteNote(note: Note) {
        //viewmodel scope because we use suspend functions
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }

    fun deleteNoteFromBin(note: Note) {
        //viewmodel scope because we use suspend functions
        viewModelScope.launch {
            noteRepository.deleteNoteFromBin(note)
        }
    }

    fun setCurrentNoteId(noteId: Long) {
        _uiState.value = _uiState.value.copy(currentNoteId = noteId)
    }

    fun setCurrentNote(note: Note) {
        _uiState.value = _uiState.value.copy(currentNote = note)
    }

    fun updateCurrentNoteTitle(title: String) {
        _uiState.value = _uiState.value.copy(currentNote = _uiState.value.currentNote.copy(title = title))
        upsertNote(_uiState.value.currentNote)
    }

    fun updateCurrentNoteBody(body: String) {
        _uiState.value = _uiState.value.copy(currentNote = _uiState.value.currentNote.copy(body = body))
        upsertNote(_uiState.value.currentNote)
    }

    fun updateCurrentNoteIsPinned(isPinned: Boolean) {
        _uiState.value = _uiState.value.copy(currentNote = _uiState.value.currentNote.copy(isPinned = isPinned))
        upsertNote(_uiState.value.currentNote)
    }

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

}