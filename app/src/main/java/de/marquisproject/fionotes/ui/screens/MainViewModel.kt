package de.marquisproject.fionotes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.marquisproject.fionotes.data.notes.model.Note
import de.marquisproject.fionotes.data.notes.model.NoteStatus
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


    fun updateNote(note: Note) {
        //viewmodel scope because we use suspend functions
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }

    fun insertAndShowNewEmptyNote() {
        viewModelScope.launch {
            val emptyNote = Note()
            val newId = noteRepository.insertNote(emptyNote)
            setCurrentNote(emptyNote.copy(id = newId))
        }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch {
            noteRepository.archiveNote(note)
        }
    }

    fun unarchiveNote (note: Note) {
        viewModelScope.launch {
            noteRepository.unarchiveNote(note)
        }
    }

    fun binNote(note: Note) {
        //viewmodel scope because we use suspend functions
        viewModelScope.launch {
            noteRepository.binNote(note)
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            noteRepository.restoreNote(note)
        }
    }

    fun deleteNoteFromBin(note: Note) {
        //viewmodel scope because we use suspend functions
        viewModelScope.launch {
            noteRepository.deleteNoteFromBin(note)
        }
    }

    fun setCurrentNote(note: Note) {
        _uiState.value = _uiState.value.copy(currentNote = note)
    }

    fun updateCurrentNoteTitle(title: String) {
        _uiState.value = _uiState.value.copy(currentNote = _uiState.value.currentNote.copy(title = title))
        updateNote(_uiState.value.currentNote)
    }

    fun updateCurrentNoteBody(body: String) {
        _uiState.value = _uiState.value.copy(currentNote = _uiState.value.currentNote.copy(body = body))
        updateNote(_uiState.value.currentNote)
    }

    fun updateCurrentNoteIsPinned(isPinned: Boolean) {
        _uiState.value = _uiState.value.copy(currentNote = _uiState.value.currentNote.copy(isPinned = isPinned))
        updateNote(_uiState.value.currentNote)
    }

    fun updateCurrentNoteStatus(noteStatus: NoteStatus) {
        _uiState.value = _uiState.value.copy(currentNote = _uiState.value.currentNote.copy(noteStatus = noteStatus))
        updateNote(_uiState.value.currentNote)
    }

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

}